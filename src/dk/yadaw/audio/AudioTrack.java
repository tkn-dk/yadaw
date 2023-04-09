package dk.yadaw.audio;

/**
 * Represents and audio track having both producer and consumer capabilities.
 * @author tkn
 *
 */
public class AudioTrack implements AudioProducer, AudioConsumer, SyncListener {
	private String name;
	private String filePath;
	private int iteration;
	private float timeCode;
	private int peakVal;
	
	public AudioTrack( String name, String filePath ) {
		this.name = name;
		this.filePath = filePath;
	}

	@Override
	public void audioSync(float timeCode, int bufferPeak) {
		this.timeCode = timeCode;
		this.peakVal = bufferPeak;
	}

	@Override
	public int read(int[] samples) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(int[] samples) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Playpack track from file.
	 * @param trackFile - Set to null for using "filePath_name_iteration.raw"
	 */
	public void play( String trackFile ) {
		
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
