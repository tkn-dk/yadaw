package dk.yadaw.audio;

/**
 * Class for holding a sample buffer and a sample position.
 * @author tkn
 *
 */
public class AudioStreamBuffer {
	private long samplePos;
	private int[] buffer;

	public AudioStreamBuffer() {
		
	}
	
	public void setBuffer( int[] buffer ) {
		this.buffer = buffer;
	}
	
	public int[] getBuffer() {
		return buffer;
	}
	
	public void setSamplePos( long pos ) {
		samplePos = pos;
	}
	
	public long getSamplePos( ) {
		return samplePos;
	}
}
