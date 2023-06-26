/**
 * YSampleContainer implementation
 * 2023-04-06/tkn@korsdal.dk
 */

#include <stdio.h>
#include "YSampleContainer.h"

YSampleContainer::YSampleContainer( uint32_t inputMask, uint32_t outputMask, size_t numberOfSamples )
{
	nofInputs = 0;
	nofOutputs = 0;

	for( uint8_t channel = 0; channel < 32; channel++ )
	{
		if( inputMask & ( 1 << channel ) )
		{
			inputChannelMap[channel] = nofInputs++;
		}
		else
		{
			inputChannelMap[channel] = 0xff;
		}

		if( outputMask & ( 1 << channel ) )
		{
			outputChannelMap[channel] = nofOutputs++;
		}
		else
		{
			outputChannelMap[channel] = 0xff;
		}
	}

	inputBuffers = new YCircBuf<long>*[nofInputs];
	outputBuffers = new YCircBuf<long>*[nofOutputs];

	for( uint8_t channel = 0; channel < 32; channel++ )
	{
		if( inputChannelMap[channel] != 0xff )
		{
			inputBuffers[inputChannelMap[channel]] = new YCircBuf<long>( numberOfSamples );
		}

		if( outputChannelMap[channel] != 0xff )
		{
			outputBuffers[outputChannelMap[channel]] = new YCircBuf<long>( numberOfSamples );
		}
	}
}

YSampleContainer::~YSampleContainer()
{
	for( uint8_t i = 0; i < nofInputs; i++ )
	{
		delete inputBuffers[i];
	}

	for( uint8_t i = 0; i < nofOutputs; i++ )
	{
		delete outputBuffers[i];
	}

	delete[] inputBuffers;
	delete[] outputBuffers;
}
