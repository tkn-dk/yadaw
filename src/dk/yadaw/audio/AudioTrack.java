package dk.yadaw.audio;

/**
 * Represents and audio track having both producer and consumer capabilities.
 * @author tkn
 *
 */
public class AudioTrack  {
	private String name;
	private String filePath;
	private int iteration;
	private float timeCode;
	private int peakVal;
	private AudioStream in;
	private AudioStream out;
	
	/**
	 * Constructor
	 * @param name Name of track
	 * @param filePath Path to store track files
	 */
	public AudioTrack( String name, String filePath ) {
		this.name = name;
		this.filePath = filePath;
		iteration = 1;
	}

	/**
	 * 
	 * @param out
	 * @param trackFile
	 */
	public void play( AudioStream out, String trackFile ) {
		
	}
	
	/**
	 * 
	 * @param in
	 * @param trackFile
	 */
	public void record( AudioStream in, String trackFile ) {
		
	}
	
	public float getTimeCode() {
		return timeCode;
	}
	
	public int getPeakVal() {
		return peakVal;
	}

	public static void main( String args[] ) {
	}
}
