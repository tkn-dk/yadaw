#include <stdio.h>
#include <windows.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include "dk_yadaw_audio_Asio.h"
#include "asiodrivers.h"
#include "asio.h"

using namespace std;

#define PLAYBACK_BUFFER_SIZE	0x20000

struct AsioContext
{
	AsioDrivers *asioDrivers;
	LPASIODRVSTRUCT asioDrvList;
	const char *driverName;
	LPASIODRVSTRUCT theOne;
	ASIODriverInfo driverInfo;
	ASIOSampleRate sampleRate;
	long numInput;
	long numOutput;
	long outputLatency;
	long inputLatency;
	long sampleBufferSize;
	long exchangeOutputBufferSize;
	long exchangeInputBufferSize;
	unsigned long long armedInputsMask;
	unsigned long long armedOutputsMask;
	long nofArmedInputs;
	long nofArmedOutputs;
	double outputLatencyMs;
	double inputLatencyMs;
	ASIOChannelInfo **inputChannelsInfo;
	ASIOChannelInfo **outputChannelsInfo;
	ASIOBufferInfo *buffers;
	ASIOCallbacks callBacks;
	ASIOSamples lastSamplePos;
	long *exchangedInputSamples;
	long *exchangedOutputSamples;
	jintArray jniReturnSampleBuffer;
};

AsioContext asioCtx = {0};

static JavaVM *jvm = NULL;
static jobject parentObject = NULL;
static jmethodID notifyMethod = NULL;

LPASIODRVSTRUCT findDriver( AsioDrivers *adrv, const string asioDriverName );
void prepBuffers( JNIEnv *env );
void prepCallbacks();
void notifySample();

/* ASIO callback functions  */
void bufferSwitch(long doubleBufferIndex, ASIOBool directProcess);
void sampleRateDidChange(ASIOSampleRate sRate);
long asioMessage(long selector, long value, void* message, double* opt);
ASIOTime* bufferSwitchTimeInfo(ASIOTime* params, long doubleBufferIndex, ASIOBool directProcess);

BOOL WINAPI DllMain(
    HINSTANCE hinstDLL,  // handle to DLL module
    DWORD fdwReason,     // reason for calling function
    LPVOID lpReserved )  // reserved
{
    // Perform actions based on the reason for calling.
    switch( fdwReason )
    {
        case DLL_PROCESS_ATTACH:
        	printf( "AsioJNI attach\n");
        	asioCtx.asioDrivers = new AsioDrivers();
            break;

        case DLL_THREAD_ATTACH:
         // Do thread-specific initialization.
            break;

        case DLL_THREAD_DETACH:
         // Do thread-specific cleanup.
            break;

        case DLL_PROCESS_DETACH:
        	printf( "AsioJNI unload\n" );
        	if( asioCtx.asioDrivers )
        	{
        		asioCtx.asioDrivers->removeCurrentDriver();
        	}
        	delete asioCtx.asioDrivers;
            break;
    }
    fflush( stdout );
    return TRUE;  // Successful DLL_PROCESS_ATTACH.
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioLibInit(JNIEnv *env, jobject thisobj)
{
	jclass parentClass;

	printf( "ASIO lib init\n" );
	env->GetJavaVM( &jvm );
	parentClass = env->FindClass( "dk/yadaw/audio/Asio" );
	parentObject = env->NewGlobalRef( thisobj );
	notifyMethod = env->GetMethodID( parentClass, "notifySample", "()V");
}

JNIEXPORT jstring JNICALL Java_dk_yadaw_audio_Asio_asioGetFirstDriver(JNIEnv *env, jobject thisobj)
{
	if( asioCtx.asioDrivers ) {
		LPASIODRVSTRUCT first = asioCtx.asioDrivers->lpdrvlist;
		asioCtx.asioDrvList = first->next;
		return env->NewStringUTF( first->drvname );
	}
	return NULL;
}

JNIEXPORT jstring JNICALL Java_dk_yadaw_audio_Asio_asioGetNextDriver(JNIEnv *env, jobject thisobj)
{
	if( asioCtx.asioDrvList )
	{
		LPASIODRVSTRUCT driver = asioCtx.asioDrvList;
		asioCtx.asioDrvList = asioCtx.asioDrvList->next;
		return env->NewStringUTF( driver->drvname );
	}
	return NULL;
}

JNIEXPORT jboolean JNICALL Java_dk_yadaw_audio_Asio_asioLoadDriver(JNIEnv *env, jobject thisobj, jstring driverName )
{
	jboolean result = JNI_FALSE;
	asioCtx.driverName = env->GetStringUTFChars( driverName, NULL );
	asioCtx.theOne = findDriver( asioCtx.asioDrivers, asioCtx.driverName );
	if( asioCtx.theOne )
	{
		if( asioCtx.asioDrivers->loadDriver( asioCtx.theOne->drvname ) )
		{
			result = JNI_TRUE;
		}
	}
	return result;
}

JNIEXPORT jboolean JNICALL Java_dk_yadaw_audio_Asio_asioInit(JNIEnv *env, jobject thisobj )
{
	LONG minSize;
	LONG maxSize;
	LONG preferredSize;
	LONG granularity;
	jboolean result = JNI_FALSE;
	asioCtx.driverInfo = {0};
	asioCtx.driverInfo.asioVersion = 2;
	ASIOError err = ASIOInit( &asioCtx.driverInfo );
	if( err == ASE_OK )
	{
		ASIOGetSampleRate( &asioCtx.sampleRate );
		ASIOError err = ASIOGetBufferSize(&minSize, &maxSize, &preferredSize, &granularity);
		asioCtx.sampleBufferSize = preferredSize;
		ASIOGetLatencies( &asioCtx.inputLatency, &asioCtx.outputLatency );
		ASIOGetChannels(&asioCtx.numInput, &asioCtx.numOutput );
		result = JNI_TRUE;
	}

	if( asioCtx.exchangedInputSamples )
	{
		delete[] asioCtx.exchangedInputSamples;
	}

	if( asioCtx.exchangedOutputSamples )
	{
		delete[] asioCtx.exchangedOutputSamples;
	}

	return result;
}

JNIEXPORT jdouble JNICALL Java_dk_yadaw_audio_Asio_asioGetSamplerate(JNIEnv *, jobject){
	return asioCtx.sampleRate;
}

JNIEXPORT jlong JNICALL Java_dk_yadaw_audio_Asio_asioGetSamplePos(JNIEnv *, jobject) {
	jlong jret = 0;
	BYTE *bjret = ( BYTE *)&jret;
	bjret[7] = (( BYTE *)&asioCtx.lastSamplePos.hi)[3];
	bjret[6] = (( BYTE *)&asioCtx.lastSamplePos.hi)[2];
	bjret[5] = (( BYTE *)&asioCtx.lastSamplePos.hi)[1];
	bjret[4] = (( BYTE *)&asioCtx.lastSamplePos.hi)[0];
	bjret[3] = (( BYTE *)&asioCtx.lastSamplePos.lo)[3];
	bjret[2] = (( BYTE *)&asioCtx.lastSamplePos.lo)[2];
	bjret[1] = (( BYTE *)&asioCtx.lastSamplePos.lo)[1];
	bjret[0] = (( BYTE *)&asioCtx.lastSamplePos.lo)[0];
	return jret;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetBufferSize(JNIEnv *, jobject)
{
	return asioCtx.sampleBufferSize;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetOutputLatency(JNIEnv *env, jobject thisobj )
{
	return asioCtx.outputLatency;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetInputLatency(JNIEnv *env, jobject thisobj )
{
	return asioCtx.inputLatency;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetAvailableInputs(JNIEnv *env, jobject thisobj )
{
	return asioCtx.numInput;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetAvailableOutputs(JNIEnv *env, jobject thisobj )
{
	return asioCtx.numOutput;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioClearArmedChannels(JNIEnv *env, jobject thisobj )
{
	delete[] asioCtx.buffers;
	delete[] asioCtx.exchangedInputSamples;
	delete[] asioCtx.exchangedOutputSamples;
	asioCtx.armedInputsMask = 0;
	asioCtx.buffers = NULL;
	asioCtx.exchangedInputSamples = NULL;
	asioCtx.exchangedOutputSamples = NULL;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioArmInput  (JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedInputsMask |= ( 1 << channel );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioArmOutput(JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedOutputsMask |= ( 1 << channel );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioPrepBuffers(JNIEnv *env, jobject thisobj )
{
	prepBuffers( env );
	prepCallbacks();
	ASIOError err = ASIOCreateBuffers( asioCtx.buffers, asioCtx.numInput, asioCtx.sampleBufferSize, &asioCtx.callBacks );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioSetOutputSamples(JNIEnv *env, jobject thisobj, jintArray samples )
{
	jint *outputSamples = env->GetIntArrayElements(samples, NULL );
	memcpy( asioCtx.exchangedOutputSamples, outputSamples, asioCtx.exchangeOutputBufferSize * sizeof( int ) );
	env->ReleaseIntArrayElements(samples, outputSamples, JNI_ABORT );
}

JNIEXPORT jintArray JNICALL Java_dk_yadaw_audio_Asio_asioGetInputSamples(JNIEnv *env, jobject thisobj )
{
	env->DeleteLocalRef( asioCtx.jniReturnSampleBuffer );
	asioCtx.jniReturnSampleBuffer = env->NewIntArray( asioCtx.exchangeInputBufferSize );

	jsize islen =  env->GetArrayLength( asioCtx.jniReturnSampleBuffer );
	if( islen != asioCtx.exchangeInputBufferSize )
	{
		printf( "ERROR: jintarray size: %u. Sample buffer size: %u.\n", islen, asioCtx.exchangeInputBufferSize );
		return NULL;
	}
	else
	{
		for( int n = 0; n < 10; n++ )
			printf( "  %i ", asioCtx.exchangedInputSamples[n] );
		printf( "\n" );

		env->SetIntArrayRegion( asioCtx.jniReturnSampleBuffer, 0, asioCtx.exchangeInputBufferSize, asioCtx.exchangedInputSamples );
	}
	return asioCtx.jniReturnSampleBuffer;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioStart(JNIEnv *, jobject)
{
	jint result = -1;
	if( asioCtx.buffers )
	{
		ASIOStart();
		result = 0;
	}
	return result;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioStop(JNIEnv *, jobject)
{
	ASIOStop();
}

void bufferSwitch(long doubleBufferIndex, ASIOBool directProcess)
{
	ASIOSamples samPos;
	ASIOTimeStamp tStamp;
	ASIOGetSamplePosition( &samPos, &tStamp );

	if( samPos.lo - asioCtx.lastSamplePos.lo > asioCtx.sampleRate )
	{
		printf( ".");
		fflush( stdout );
		asioCtx.lastSamplePos = samPos;
	}
}

void sampleRateDidChange(ASIOSampleRate sRate)
{
	printf( "\nNew sample rate: %f", sRate );
}

long asioMessage(long selector, long value, void* message, double* opt )
{
	switch( selector )
	{
	case kAsioSelectorSupported:
		return ASIOTrue;

	case kAsioEngineVersion:
		return 2;

	case kAsioSupportsTimeInfo:
		return ASIOTrue;

	case kAsioSupportsTimeCode:
		return ASIOTrue;

	case kAsioResetRequest:
		return ASIOTrue;

	default:
		printf( "asioMessage unhandled: %i", selector );
		break;
	}
	return 0;
}

ASIOTime* bufferSwitchTimeInfo(ASIOTime* params, long doubleBufferIndex, ASIOBool directProcess)
{
	DWORD byteWritten;
	DWORD bytesToWrite = 3 * asioCtx.sampleBufferSize;
	double timePos  = params->timeInfo.samplePosition.lo / asioCtx.sampleRate;
	asioCtx.lastSamplePos = params->timeInfo.samplePosition;
	notifySample();
	return params;
}

LPASIODRVSTRUCT findDriver( AsioDrivers *adrv, const string asioDriverName )
{
	LPASIODRVSTRUCT drvParse = adrv->lpdrvlist;
	LPASIODRVSTRUCT theOne = NULL;

	int n = 1;
	while( drvParse )
	{
		string driverName = drvParse->drvname;
		if( asioDriverName.length() > 0 && asioDriverName.find( driverName ) != string::npos )
		{
			theOne = drvParse;
			break;
		}
		drvParse = drvParse->next;
		n++;
	}
	return theOne;
}

bool getChannelInfo( AsioContext *ctx )
{
	ctx->inputChannelsInfo = new ASIOChannelInfo*[ctx->numInput];
	for( int in = 0; in < ctx->numInput; in++ )
	{
		ctx->inputChannelsInfo[in] = new ASIOChannelInfo;
		*(ctx->inputChannelsInfo[in]) = {0};
		ctx->inputChannelsInfo[in]->channel = in;
		ctx->inputChannelsInfo[in]->isInput = ASIOTrue;
		ASIOGetChannelInfo( ctx->inputChannelsInfo[in]);
	}

	ctx->outputChannelsInfo = new ASIOChannelInfo*[ctx->numOutput];
	for( int out = 0; out < ctx->numOutput; out++ )
	{
		ctx->outputChannelsInfo[out] = new ASIOChannelInfo;
		*(ctx->outputChannelsInfo[out]) = {0};
		ctx->outputChannelsInfo[out]->channel = out;
		ctx->outputChannelsInfo[out]->isInput = ASIOFalse;
		ASIOGetChannelInfo( ctx->outputChannelsInfo[out] );
	}

	return true;
}

void prepBuffers( JNIEnv *env )
{

	int nofArmedOutputs = 0;
	int nofArmedInputs = 0;
	unsigned long long mask = 1;
	while ( mask != 0 )
	{
		if( asioCtx.armedInputsMask & mask )
		{
			nofArmedInputs++;
		}

		if( asioCtx.armedOutputsMask & mask )
		{
			nofArmedOutputs++;
		}

		mask = ( mask << 1 );
	}

	int numChannels = nofArmedInputs + nofArmedOutputs;

	int bufIndex = 0;
	asioCtx.buffers = new ASIOBufferInfo[numChannels];
	int n = 0;
	mask = 1;
	while( mask != 0 )
	{
		if( asioCtx.armedInputsMask & mask )
		{
			asioCtx.buffers[bufIndex] = {0};
			asioCtx.buffers[bufIndex].channelNum = n;
			asioCtx.buffers[bufIndex].isInput = ASIOTrue;
			bufIndex++;
		}

		mask = ( mask << 1 );
		n++;
	}

	n = 0;
	mask = 1;
	while( mask != 0 )
	{
		if( asioCtx.armedOutputsMask & mask )
		{
			asioCtx.buffers[bufIndex] = {0};
			asioCtx.buffers[bufIndex].channelNum = n;
			asioCtx.buffers[bufIndex].isInput = ASIOFalse;
			bufIndex++;
		}

		mask = ( mask << 1 );
		n++;
	}

	asioCtx.nofArmedInputs = nofArmedInputs;
	asioCtx.nofArmedOutputs = nofArmedOutputs;
	asioCtx.exchangeInputBufferSize = nofArmedInputs * asioCtx.sampleBufferSize;
	asioCtx.exchangeOutputBufferSize = nofArmedOutputs * asioCtx.sampleBufferSize;
	if( asioCtx.exchangedInputSamples )
	{
		delete[] asioCtx.exchangedInputSamples;
	}

	if( asioCtx.exchangedOutputSamples )
	{
		delete[] asioCtx.exchangedOutputSamples;
	}

	asioCtx.exchangedInputSamples = new long[asioCtx.exchangeInputBufferSize];
	asioCtx.exchangedOutputSamples = new long[asioCtx.exchangeOutputBufferSize];
	asioCtx.jniReturnSampleBuffer = env->NewIntArray( asioCtx.exchangeInputBufferSize );

	printf( "jni: prepBuffer. %u input. %u output\n", nofArmedInputs, nofArmedOutputs );

}

void prepCallbacks()
{
	printf( "jni prepCallbacks\n" );
	asioCtx.callBacks.asioMessage = asioMessage;
	asioCtx.callBacks.bufferSwitch = bufferSwitch;
	asioCtx.callBacks.bufferSwitchTimeInfo = bufferSwitchTimeInfo;
	asioCtx.callBacks.sampleRateDidChange = sampleRateDidChange;
}

void notifySample()
{
    JNIEnv* env;
    int getEnvResult = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvResult == JNI_EDETACHED) {
        jvm->AttachCurrentThread((void**)&env, NULL);
    } else if (getEnvResult == JNI_EVERSION) {
        printf( "JNI version error\n" );
    }

    env->MonitorEnter( parentObject );
    env->CallVoidMethod(parentObject, notifyMethod);
    env->MonitorExit( parentObject );

    if (getEnvResult == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
}
