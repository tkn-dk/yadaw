package dk.yadaw.audio;

import java.util.Set;

/**
 * SoundProcessor can process sound between one or more inputs and outputs.
 * The soundprocessor also has a method 
 * @author tkn
 *
 */
public abstract class SoundProcessor {
	private Set<SoundInStream> inputs;
	private Set<SoundOutStream> outputs;
	
	/**
	 * @param inputs	Sound inputs to this sound processor.
	 * @param outputs	Sound outputs from this sound process.
	 */
	public SoundProcessor( Set<SoundInStream> inputs, Set<SoundOutStream> outputs ) {
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	/**
	 * The sound processor should process the next input stream buffer, and add output to output stream.
	 * Input streams may contain more samples than just the next to process - e.g. streaming a track from 
	 * an input file could fill buffers ahead. Processing samples must be done synchronous with output, 
	 * also, any VU meter may be updated from call to process.
	 * @param samplePosition	Current sample position in number of samples since processing was started.
	 * @param syncBuffer		Copy of sample data being 
	 */
	public abstract void process( long samplePosition );

}
