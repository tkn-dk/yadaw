/**
 * YSampleContainer
 * Holds a vector of YCircBuffers for input channels and one
 * for output channels.
 * 2023-04-06/tkn@korsdal.dk
 */
#include <stdint.h>
#include <stddef.h>
#include "YCircBuf.h"

class YSampleContainer
{
public:
	YSampleContainer( uint32_t inputMask, uint32_t outputMask, size_t numberOfSamples );
	~YSampleContainer();

	bool extractInputSample( uint32_t channel, long& sample )
	{
		return inputBuffers[inputChannelMap[channel]]->read( sample );
	}

	bool extractOutputSample( uint32_t channel, long& sample )
	{
		return outputBuffers[outputChannelMap[channel]]->read( sample );
	}

	bool addInputSample( uint32_t channel, long sample )
	{
		return inputBuffers[inputChannelMap[channel]]->write( sample );
	}

	bool addOutputSample( uint32_t channel, long sample )
	{
		return outputBuffers[outputChannelMap[channel]]->write( sample );
	}

	YCircBuf<long> **getInputBuffers()
	{
		return inputBuffers;
	}

	YCircBuf<long> **getOutputBuffers()
	{
		return outputBuffers;
	}

private:
	uint32_t nofInputs;
	uint32_t nofOutputs;
	uint8_t inputChannelMap[32];
	uint8_t outputChannelMap[32];
	YCircBuf<long> **inputBuffers;
	YCircBuf<long> **outputBuffers;

};
