package dk.yadaw.audio;

/**
 * PeakListener can be added to an AudioStream, and will be called
 * with the peak value of the last buffer read from the AudioStream
 * @author tkn
 *
 */
public interface PeakListener {
	public void peakUpdate( int peak );
}
