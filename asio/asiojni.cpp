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
	LONG channel;
	LPASIODRVSTRUCT theOne;
	ASIODriverInfo driverInfo;
	ASIOSampleRate sampleRate;
	LONG numInput;
	LONG numOutput;
	LONG bufferSize;
	LONG outputLatency;
	LONG inputLatency;
	unsigned long long armedInputsMask;
	unsigned long long armedOutputsMask;
	LONG nofArmedInputs;
	LONG nofArmedOutputs;
	double outputLatencyMs;
	double inputLatencyMs;
	ASIOChannelInfo **inputChannelsInfo;
	ASIOChannelInfo **outputChannelsInfo;
	ASIOBufferInfo *buffers;
	ASIOCallbacks callBacks;
	ASIOSamples lastSamplePos;
	long *exchangedInputSamples;
	long *exchangedOutputSamples;
};

AsioContext asioCtx = {0};

LPASIODRVSTRUCT findDriver( AsioDrivers *adrv, const string asioDriverName );
void prepBuffers();
void prepCallbacks();

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
        	delete asioCtx.asioDrivers;
            break;
    }
    return TRUE;  // Successful DLL_PROCESS_ATTACH.
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioLibInit(JNIEnv *env, jobject thisobj)
{
}

JNIEXPORT jstring JNICALL Java_dk_yadaw_audio_Asio_asioGetFirstDriver(JNIEnv *env, jobject thisobj)
{
	LPASIODRVSTRUCT first = asioCtx.asioDrivers->lpdrvlist;
	asioCtx.asioDrvList = first;
	return env->NewStringUTF( first->drvname );
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
		asioCtx.bufferSize = preferredSize;
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

JNIEXPORT jint JNICALL Java_dk_yadaw_audio_Asio_asioGetBufferSize(JNIEnv *, jobject)
{
	return asioCtx.bufferSize;
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

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioPrepBuffers(JNIEnv *, jobject)
{
	prepBuffers();
	prepCallbacks();
	ASIOError err = ASIOCreateBuffers( asioCtx.buffers, asioCtx.numInput, asioCtx.bufferSize, &asioCtx.callBacks );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioSetOutputSamples(JNIEnv *env, jobject thisobj, jintArray samples )
{
	jint *outputSamples = env->GetIntArrayElements(samples, NULL );

	memcpy( asioCtx.exchangedOutputSamples, outputSamples, asioCtx.bufferSize * asioCtx.nofArmedOutputs * sizeof( int ));
	env->ReleaseIntArrayElements(samples, outputSamples, JNI_ABORT );
}

JNIEXPORT jintArray JNICALL Java_dk_yadaw_audio_Asio_asioGetInputSamples(JNIEnv *env, jobject thisobj )
{
	return NULL;
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
	DWORD bytesToWrite = 3 * asioCtx.bufferSize;
	double timePos  = params->timeInfo.samplePosition.lo / asioCtx.sampleRate;

	asioCtx.lastSamplePos = params->timeInfo.samplePosition;
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

void prepBuffers()
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
	if( asioCtx.exchangedInputSamples )
	{
		delete[] asioCtx.exchangedInputSamples;
	}

	if( asioCtx.exchangedOutputSamples )
	{
		delete[] asioCtx.exchangedOutputSamples;
	}

	asioCtx.exchangedInputSamples = new long[nofArmedInputs * asioCtx.bufferSize];
	asioCtx.exchangedOutputSamples = new long[nofArmedOutputs * asioCtx.bufferSize];
}

void prepCallbacks()
{
	asioCtx.callBacks.asioMessage = asioMessage;
	asioCtx.callBacks.bufferSwitch = bufferSwitch;
	asioCtx.callBacks.bufferSwitchTimeInfo = bufferSwitchTimeInfo;
	asioCtx.callBacks.sampleRateDidChange = sampleRateDidChange;
}
