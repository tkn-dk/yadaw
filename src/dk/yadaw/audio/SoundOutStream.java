package dk.yadaw.audio;

/**
 * 
 * Interface representing an output stream.
 * @author tkn
 */
public interface SoundOutStream {
	/**
	 * Write samples to the output stream.
	 * @param samples  	Buffer of samples as 32 bit left adjusted ints.
	 * @return			Number of samples written. Will always be current system buffersize
	 * 					or 0 if stream is full.
	 */
	public int write( int[] samples );
}
