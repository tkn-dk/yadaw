package dk.yadaw.audio;

/**
 * Represents the interface for sound input.
 */
public interface AudioConsumer {
	/**
	 * Reads next buffer of samples. Can be called continously to empty the stream.
	 * 
	 * @param samples	Buffer with samples as 32 bit left adjusted ints
	 * @return			Number of samples read. 0 if no more samples is available.
	 */
	public int read( int[] samples );
}
