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
	bool record;
	bool playing;
	LONG channel;
	char *fileName;
	char *driverName;
	BYTE *recordFileBuffer;
	BYTE *playbackBuffer[2];
	DWORD playbackBufferRead[2];
	DWORD playbackBufferSize;
	DWORD playbackBufferIndex;
	DWORD playbackOut;
	bool playbackEOF;
	bool playbackFinished;
	HANDLE recordFileHandle;
	HANDLE playbackFileHandle;
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
	double outputLatencyMs;
	double inputLatencyMs;
	ASIOChannelInfo **inputChannelsInfo;
	ASIOChannelInfo **outputChannelsInfo;
	ASIOBufferInfo *buffers;
	ASIOCallbacks callBacks;
	ASIOSamples lastSamplePos;
};

AsioContext asioCtx = {0};

LPASIODRVSTRUCT findDriver( AsioDrivers *adrv, const string asioDriverName );


BOOL WINAPI DllMain(
    HINSTANCE hinstDLL,  // handle to DLL module
    DWORD fdwReason,     // reason for calling function
    LPVOID lpReserved )  // reserved
{
    // Perform actions based on the reason for calling.
    switch( fdwReason )
    {
        case DLL_PROCESS_ATTACH:
        	asioCtx.asioDrivers = new AsioDrivers();
            break;

        case DLL_THREAD_ATTACH:
         // Do thread-specific initialization.
            break;

        case DLL_THREAD_DETACH:
         // Do thread-specific cleanup.
            break;

        case DLL_PROCESS_DETACH:
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

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioClearUsedInputs(JNIEnv *env, jobject thisobj )
{
	asioCtx.armedInputsMask = 0;
	// TODO: Delete assigned buffers
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioClearUsedOutputs(JNIEnv *env, jobject thisobj )
{
	asioCtx.armedOutputsMask = 0;
	// TODO Delete assigned output buffers
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioActivateInput  (JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedInputsMask |= ( 1 << channel );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioActivateOutput(JNIEnv *env, jobject thisobj, jint channel )
{
	asioCtx.armedOutputsMask |= ( 1 << channel );
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioPrepBuffers(JNIEnv *, jobject)
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
}

JNIEXPORT jintArray JNICALL Java_dk_yadaw_audio_Asio_asioExchangeBuffers(JNIEnv *, jobject, jintArray)
{
	return NULL;
}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioStart(JNIEnv *, jobject)
{
	ASIOStart();

}

JNIEXPORT void JNICALL Java_dk_yadaw_audio_Asio_asioStop(JNIEnv *, jobject)
{
	ASIOStop();
}


/* ASIO callback functions  */
void bufferSwitch(long doubleBufferIndex, ASIOBool directProcess);
void sampleRateDidChange(ASIOSampleRate sRate);
long asioMessage(long selector, long value, void* message, double* opt);
ASIOTime* bufferSwitchTimeInfo(ASIOTime* params, long doubleBufferIndex, ASIOBool directProcess);

void fillPlaybackBuffer( DWORD pbufIndex );

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

	if( asioCtx.recordFileHandle )
	{
		int fileBufIndex = 0;
		for( int i = 0; i < asioCtx.bufferSize; i++ )
		{
			unsigned int sample = *( ( unsigned int *)asioCtx.buffers[asioCtx.channel].buffers[doubleBufferIndex] + i );
			asioCtx.recordFileBuffer[fileBufIndex++] = ( BYTE )( sample >> 24 );
			asioCtx.recordFileBuffer[fileBufIndex++] = ( BYTE )( sample >> 16 );
			asioCtx.recordFileBuffer[fileBufIndex++] = ( BYTE )( sample >> 8 );
		}
		printf( "\rRecording: %8.3f", timePos );
		fflush( stdout );
		WriteFile( asioCtx.recordFileHandle, asioCtx.recordFileBuffer, bytesToWrite, &byteWritten, NULL );
	}
	else if( asioCtx.playbackFileHandle && !asioCtx.playbackFinished )
	{
		unsigned int sample;
		for( int i = 0; i < asioCtx.bufferSize; i++ )
		{
			sample = 0;
			for( int s = 0; s < 3; s++  )
			{
				sample = ( sample << 8 ) + asioCtx.playbackBuffer[asioCtx.playbackBufferIndex][asioCtx.playbackOut++];
			}

			sample = sample << 8;
			*( ( unsigned int *)asioCtx.buffers[asioCtx.channel].buffers[doubleBufferIndex] + i ) = sample;


			if( asioCtx.playbackOut >= asioCtx.playbackBufferRead[asioCtx.playbackBufferIndex] )
			{
				if( asioCtx.playbackEOF )
				{
					printf( "\nPlayback finished." );
					fflush( stdout );
					asioCtx.playbackFinished = true;
					ASIOStop();
					break;
				}
				else
				{
					asioCtx.playbackOut = 0;
					fillPlaybackBuffer( asioCtx.playbackBufferIndex );
					asioCtx.playbackBufferIndex = ( asioCtx.playbackBufferIndex + 1 ) & 1 ;
				}
			}
			ASIOOutputReady();
		}
		//printf( "\rPlayback: %8.3f", timePos );
		//fflush( stdout );
	}
	asioCtx.lastSamplePos = params->timeInfo.samplePosition;
	return params;
}

void printChannelInfo( ASIOChannelInfo *ainfo )
{
	printf( "\n  %i: %s. Group: %i  Type: %i", ainfo->channel, ainfo->name, ainfo->channelGroup, ainfo->type );
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

bool getAsioInfo( AsioContext *ctx )
{
	ctx->driverInfo = {0};
	ctx->driverInfo.asioVersion = 2;
	ASIOError err = ASIOInit( &ctx->driverInfo );
	if( err != ASE_OK )
	{
		return false;
	}
	else
	{
		const ASIOSampleRate setRate = 48000.0;
		ASIOSetSampleRate( setRate );
		ASIOGetSampleRate( &ctx->sampleRate );
		ASIOGetChannels(&ctx->numInput, &ctx->numOutput );
		ASIOGetLatencies( &ctx->inputLatency, &ctx->outputLatency );
		ctx->inputLatencyMs = ( 1000.0 * ctx->inputLatency ) / ctx->sampleRate;
		ctx->outputLatencyMs = ( 1000.0 * ctx->outputLatency ) / ctx->sampleRate;

		LONG minSize;
		LONG maxSize;
		LONG preferredSize;
		LONG granularity;
		err = ASIOGetBufferSize(&minSize, &maxSize, &preferredSize, &granularity);
		ctx->bufferSize = preferredSize;

		getChannelInfo( ctx );
	}
	return true;
}

bool prepBuffers( AsioContext *ctx )
{
	if( asioCtx.record )
	{
		ctx->buffers = new ASIOBufferInfo[ctx->numInput];
		for( int channel = 0; channel < ctx->numInput; channel++ )
		{
			ctx->buffers[channel] = {0};
			ctx->buffers[channel].channelNum = channel;
			ctx->buffers[channel].isInput = ASIOTrue;
		}
	}
	else
	{
		ctx->buffers = new ASIOBufferInfo[ctx->numOutput];
		for( int channel = 0; channel < ctx->numOutput; channel++ )
		{
			ctx->buffers[channel] = {0};
			ctx->buffers[channel].channelNum = channel;
			ctx->buffers[channel].isInput = ASIOFalse;
		}
	}

	return true;
}

bool prepCallbacks( AsioContext *ctx )
{
	ctx->callBacks.asioMessage = asioMessage;
	ctx->callBacks.bufferSwitch = bufferSwitch;
	ctx->callBacks.bufferSwitchTimeInfo = bufferSwitchTimeInfo;
	ctx->callBacks.sampleRateDidChange = sampleRateDidChange;
	return true;
}

void fillPlaybackBuffer( DWORD pbufIndex )
{
	ReadFile( asioCtx.playbackFileHandle, asioCtx.playbackBuffer[pbufIndex], asioCtx.playbackBufferSize, &asioCtx.playbackBufferRead[pbufIndex], NULL );
	if( asioCtx.playbackBufferRead[pbufIndex] < asioCtx.playbackBufferSize )
	{
		asioCtx.playbackEOF = true;
	}
	printf( "\nTo read: %uRead: %u", asioCtx.playbackBufferSize, asioCtx.playbackBufferRead[pbufIndex] );
	fflush( stdout );
}

void listDrivers( void )
{
	LPASIODRVSTRUCT drvParse = asioCtx.asioDrivers->lpdrvlist;
	int dn = 1;
	printf( "\nAvailable ASIO drivers: ");
	while( drvParse )
	{
		printf( "\n%u %s", dn, drvParse->drvname );
		drvParse = drvParse->next;
		dn++;
	}
	printf( "\n" );
}

void printUsage( void )
{
	printf( "\nTo playback or record a file: ");
	printf( "\nasiojni [record] -ch <channel number> -d <driver name> -f <filename>");
	printf( "\nTo list files: ");
	printf( "\nasiojni -l");
}

int main( int argc, char *args[] )
{

	int arg = 1;
	while( arg < argc )
	{
		if( strstr( args[arg], "-d") )
		{
			asioCtx.driverName = args[arg+1];
			arg += 2;
		}
		else if( strstr( args[arg], "-f" ) )
		{
			asioCtx.fileName = args[arg+1];
			arg += 2;
		}
		else if( strstr( args[arg], "-ch" ))
		{
			asioCtx.channel = atoi( args[arg+1] );
			arg += 2;
		}
		else if( strstr( args[arg], "record" ))
		{
			asioCtx.record = true;
			arg++;
		}
		else if( strstr( args[arg], "-l" ))
		{
			listDrivers();
			arg++;
		}
		else
		{
			printUsage();
			delete asioCtx.asioDrivers;
			return 1;
		}
	}

	if( ( asioCtx.driverName == nullptr ) || ( asioCtx.fileName == nullptr ) )
	{
		printUsage();
		delete asioCtx.asioDrivers;
		return 1;
	}

	asioCtx.theOne = findDriver( asioCtx.asioDrivers, asioCtx.driverName, "" );

	if( asioCtx.theOne )
	{
		if( asioCtx.asioDrivers->loadDriver( asioCtx.theOne->drvname ) )
		{
			if( getAsioInfo( &asioCtx ) )
			{
				printf( "\nDriver: %s", asioCtx.theOne->drvname );
				printf( "\nNumber of inputs: %u", asioCtx.numInput );
				printf( "\nNumber of outputs: %u", asioCtx.numOutput );
				printf( "\nSample rate: %f", asioCtx.sampleRate );
				printf( "\nInput latency: %fms", asioCtx.inputLatencyMs );
				printf( "\nOutput latency: %fms", asioCtx.outputLatencyMs );
				printf( "\nBuffer size: %u", asioCtx.bufferSize );

				printf( "\n  Inputs: ");
				for( int in = 0; in < asioCtx.numInput; in++ )
				{
					printChannelInfo( asioCtx.inputChannelsInfo[in ]);
				}

				printf( "\n  Outputs: " );
				for( int out = 0; out < asioCtx.numOutput; out++ )
				{
					printChannelInfo( asioCtx.outputChannelsInfo[out] );
				}

				prepBuffers( &asioCtx );
				prepCallbacks( &asioCtx );
				ASIOError err = ASIOCreateBuffers( asioCtx.buffers, asioCtx.numInput, asioCtx.bufferSize, &asioCtx.callBacks );
				printf( "\nASIOCreateBuffers returned: %i", err );

				if( asioCtx.record )
				{
					asioCtx.recordFileHandle = CreateFile( asioCtx.fileName, GENERIC_WRITE, FILE_SHARE_READ, NULL,
							CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL );
					if( asioCtx.recordFileHandle == INVALID_HANDLE_VALUE )
					{
						delete asioCtx.asioDrivers;
						printf( "\nInvalid file");
						return 1;
					}
					asioCtx.recordFileBuffer = new BYTE [3*asioCtx.bufferSize];
					printf( "\nPress key to terminate recording\n " );
				}
				else
				{
					// Playback - open file and fill buffer
					asioCtx.playbackFileHandle = CreateFile( asioCtx.fileName, GENERIC_READ, FILE_SHARE_WRITE, NULL,
							OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL );
					if( asioCtx.playbackFileHandle != INVALID_HANDLE_VALUE )
					{
						asioCtx.playbackBufferSize = 3 * asioCtx.sampleRate;
						asioCtx.playbackBuffer[0] = new BYTE[asioCtx.playbackBufferSize];
						asioCtx.playbackBuffer[1] = new BYTE[asioCtx.playbackBufferSize];
						fillPlaybackBuffer( 0 );
						fillPlaybackBuffer( 1 );
					}
					else
					{
						printf( "\nFailed opening file %s", asioCtx.fileName );
						return 1;
					}
				}

				err = ASIOStart();
				fflush( stdout );

				if( asioCtx.record )
				{
					getchar();
					ASIOStop();

					CloseHandle( asioCtx.recordFileHandle );
					printf( "\nRecording to %s done.", asioCtx.fileName );
				}
				else
				{
					while( !asioCtx.playbackFinished ) {
						Sleep( 100 );
					}

					CloseHandle( asioCtx.playbackFileHandle );
					printf( "\nPLayback from %s done.", asioCtx.fileName );

					delete[] asioCtx.playbackBuffer[0];
					delete[] asioCtx.playbackBuffer[1];
				}


			}
			else
			{
				printf( "Getting ASIO info failed" );
			}

		}
		else
		{
			printf( "\nCould not load driver %s", asioCtx.theOne->drvname );
		}
	}
	else
	{
		printf( "\nDriver not found" );
	}
	return 0;
}
