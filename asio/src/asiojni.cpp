#include <stdio.h>
#include <windows.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <stdint.h>
#include "YSampleContainer.h"
#include "dk_yadaw_audio_Asio.h"
#include "asiodrivers.h"
#include "asio.h"

using namespace std;

#define CIRC_BUFFER_SIZE	4096

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
	long armedInputsMask;
	long armedOutputsMask;
	long nofArmedInputs;
	long nofArmedOutputs;
	double outputLatencyMs;
	double inputLatencyMs;
	double timePos;
	ASIOChannelInfo **inputChannelsInfo;
	ASIOChannelInfo **outputChannelsInfo;
	ASIOBufferInfo *buffers;
	ASIOCallbacks callBacks;
	ASIOSamples lastSamplePos;
	jintArray jniReturnSampleBuffer;
	YSampleContainer *sampleContainer;
	bool isFirstInputSampleBuf;				// First buffer with input samples is crap.
	JavaVM *jvm;
	jobject monitorObject;
	jmethodID notifyMethod;
};

AsioContext asioCtx;


LPASIODRVSTRUCT findDriver( AsioDrivers *adrv, const string asioDriverName );
void errString( ASIOError err );
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
        	if( asioCtx.asioDrivers )
        	{
        		delete asioCtx.asioDrivers;
        	}
        	asioCtx = {0};
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
        	/*if( asioCtx.asioDrivers )
        	{
        		asioCtx.asioDrivers->removeCurrentDriver();
        	}
        	delete asioCtx.asioDrivers;
        	*/
            break;
    }
    fflush( stdout );
    return TRUE;  // Successful DLL_PROCESS_ATTACH.
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioLibInit(JNIEnv *env, jobject thisobj)
{
	jclass parentClass;

	printf( "ASIO lib init\n" );
	env->GetJavaVM( &asioCtx.jvm );
	parentClass = env->FindClass( "dk/yadaw/audio/Asio" );
	asioCtx.monitorObject = env->NewGlobalRef( thisobj );
	asioCtx.notifyMethod = env->GetMethodID( parentClass, "notifySample", "()V");
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
	if( asioCtx.buffers )
	{
		delete[] asioCtx.buffers;
	}

	if( asioCtx.sampleContainer )
	{
		delete asioCtx.sampleContainer;
	}

	asioCtx.sampleContainer = nullptr;
	asioCtx.armedInputsMask = 0;
	asioCtx.armedOutputsMask = 0;
	asioCtx.numInput = 0;
	asioCtx.numOutput = 0;
	asioCtx.buffers = nullptr;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioArmInput  (JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedInputsMask |= ( 1 << channel );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioArmOutput(JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedOutputsMask |= ( 1 << channel );
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioSetOutputSamples(JNIEnv *env, jobject thisobj, jobjectArray outputSamples )
{
	jsize nofIntBuffers = env->GetArrayLength( outputSamples );
	int nofSamples = 0;

	if( nofIntBuffers == asioCtx.nofArmedOutputs )
	{
		YCircBuf<long> **outputBuffers = asioCtx.sampleContainer->getOutputBuffers();
	    jintArray *chs = new jintArray[nofIntBuffers];

	    for (int ch = 0; ch < nofIntBuffers; ch++)
	    {
	        chs[ch] = (jintArray) env->GetObjectArrayElement(outputSamples, ch);
			if (chs[ch] != nullptr )
			{
				jint *samples = env->GetIntArrayElements(chs[ch], NULL);
				for (int sample = 0; sample < env->GetArrayLength(chs[ch]);	sample++)
				{
					long s = samples[sample];
					if (!outputBuffers[ch]->write(s))
					{
						nofSamples = sample;
						break;
					}
				}
				env->ReleaseIntArrayElements(chs[ch], samples, 0);
			}
		}
	    delete[] chs;
	}
	else if( asioCtx.nofArmedOutputs > 0 )
	{
		printf( "Number of buffers (%u) does not match number of armed outputs (%u)\n", nofIntBuffers, asioCtx.nofArmedOutputs );
	}
	return nofSamples;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetInputSamples(JNIEnv *env, jobject thisobj, jobjectArray inputSamples )
{
	jsize nofIntBuffers =  env->GetArrayLength( inputSamples );
    int sampleCount = 0;

	if( nofIntBuffers == asioCtx.nofArmedInputs )
	{
		YCircBuf<long> **inputBuffers = asioCtx.sampleContainer->getInputBuffers();
	    jintArray *chs = new jintArray[nofIntBuffers];

	    for (int ch = 0; ch < nofIntBuffers; ch++) {
	        chs[ch] = (jintArray) env->GetObjectArrayElement(inputSamples, ch);
	        jint *samples = env->GetIntArrayElements(chs[ch], NULL);
	        int inputArrayLength =  env->GetArrayLength(chs[ch]);
	        while( sampleCount < inputArrayLength )
	        {
	        	long s;
	        	if( inputBuffers[ch]->read( s ) )
	        	{
	        		samples[sampleCount] = s;
	        	}
	        	else
	        	{
	        		break;
	        	}
	        	sampleCount++;
	        }
	        env->ReleaseIntArrayElements(chs[ch], samples, 0);
	    }
	    delete[] chs;
	}
	else
	{
		printf( "Number of buffers (%u) does not match number of armed inputs (%u)\n", nofIntBuffers, asioCtx.nofArmedInputs );
	}
	return sampleCount;
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioFreeOutputSamples(JNIEnv *env, jobject, jint channel )
{
	return ( jint )asioCtx.sampleContainer->availableOutputBufferCapacity( channel );
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioFreeInputSamples(JNIEnv *end, jobject, jint channel )
{
	return (jint )asioCtx.sampleContainer->availableInputBufferCapacity( channel );
}

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioPrepBuffers(JNIEnv *env, jobject thisobject )
{
	int nofArmedOutputs = 0;
	int nofArmedInputs = 0;
	unsigned int mask = 1;
	if( asioCtx.armedInputsMask == 0 && asioCtx.armedOutputsMask == 0 )
	{
		printf( "JNI asioPrepBuffer: No inputs or outputs set\n");
		return (jint )-1;
	}

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
	asioCtx.isFirstInputSampleBuf = true;

	prepCallbacks();

	ASIOError err = ASIOCreateBuffers( asioCtx.buffers, numChannels, asioCtx.sampleBufferSize, &asioCtx.callBacks );
	if( err != ASE_OK && err != ASE_SUCCESS )
	{
		errString( err );
		return ( jint )-1;
	}

	if( asioCtx.sampleContainer != nullptr )
	{
		delete asioCtx.sampleContainer;
		asioCtx.sampleContainer = nullptr;
	}

	asioCtx.sampleContainer = new YSampleContainer( asioCtx.armedInputsMask, asioCtx.armedOutputsMask, CIRC_BUFFER_SIZE );

	printf( "\nJNI  asioPrepBuffer: Nof inputs: %u, nof outputs %u\n", asioCtx.nofArmedInputs, asioCtx.nofArmedOutputs );

	return 0;
}


JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioStart(JNIEnv *, jobject)
{
	ASIOStart();
	return ( jint)0;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioStop(JNIEnv *, jobject)
{
	ASIOStop();
}

void errString( ASIOError err )
{
	switch( err )
	{
	case ASE_OK:
	case ASE_SUCCESS:
		printf( "OK\n");
		break;

	case ASE_NotPresent:
		printf( "NotPresent\n");
		break;

	case ASE_HWMalfunction:
		printf( "HW malfunction\n");
		break;

	case ASE_InvalidParameter:
		printf( "Invalid parameter\n");
		break;

	case ASE_InvalidMode:
		printf( "Invalid mode\n" );
		break;

	case ASE_SPNotAdvancing:
		printf( "SP Not advancing\n" );
		break;

	case ASE_NoClock:
		printf( "No Clock\n" );
		break;

	case ASE_NoMemory:
		printf( "No memory\n" );
		break;

	default:
		printf( "?\n");
		break;
	}
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
	// TODO: Should hi part of samplepos be taken into account (recordings > 24h)
	asioCtx.timePos  = params->timeInfo.samplePosition.lo / asioCtx.sampleRate;
	asioCtx.lastSamplePos = params->timeInfo.samplePosition;

	int nofChannels = asioCtx.nofArmedInputs + asioCtx.nofArmedOutputs;
	for( int bix = 0; bix < nofChannels; bix++ )
	{
		if( asioCtx.buffers[bix].isInput )
		{
			if( !asioCtx.isFirstInputSampleBuf )
			{
				for( int s = 0; s < asioCtx.sampleBufferSize; s++ )
				{
					asioCtx.sampleContainer->addInputSample( asioCtx.buffers[bix].channelNum, *(( long *)asioCtx.buffers[bix].buffers[doubleBufferIndex] + s));
				}
			}
		}
		else
		{
			for( int s = 0; s < asioCtx.sampleBufferSize; s++ )
			{
				long sample;
				bool hasSample = asioCtx.sampleContainer->extractOutputSample( asioCtx.buffers[bix].channelNum, sample );
				*( ( long *)asioCtx.buffers[bix].buffers[doubleBufferIndex] + s ) = hasSample ? sample : 0;
			}
		}
	}

	ASIOOutputReady();

	if( !asioCtx.isFirstInputSampleBuf )
	{
		notifySample();
	}
	else
	{
		asioCtx.isFirstInputSampleBuf = false;
	}
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

void prepCallbacks()
{
	asioCtx.callBacks.asioMessage = asioMessage;
	asioCtx.callBacks.bufferSwitch = bufferSwitch;
	asioCtx.callBacks.bufferSwitchTimeInfo = bufferSwitchTimeInfo;
	asioCtx.callBacks.sampleRateDidChange = sampleRateDidChange;
}

void notifySample()
{
    JNIEnv* env;
    int getEnvResult = asioCtx.jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvResult == JNI_EDETACHED) {
        asioCtx.jvm->AttachCurrentThread((void**)&env, NULL);
    } else if (getEnvResult == JNI_EVERSION) {
        printf( "JNI version error\n" );
    }

    env->MonitorEnter( asioCtx.monitorObject );
    env->CallVoidMethod( asioCtx.monitorObject, asioCtx.notifyMethod);
    env->MonitorExit( asioCtx.monitorObject );

    if (getEnvResult == JNI_EDETACHED) {
        asioCtx.jvm->DetachCurrentThread();
    }
}
