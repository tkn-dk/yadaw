package dk.yadaw.main;

import java.io.IOException;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.AudioTrack;
import dk.yadaw.audio.MixerChannel;
import dk.yadaw.audio.SyncListener;
import dk.yadaw.widgets.TrackPanel;
import dk.yadaw.widgets.VUMeter;

/**
 * Controller for on track/mixerchannel.
 * @author tkn
 *
 */
public class TrackController implements SyncListener {
	private MixerChannel mixerChannel;
	private TrackPanel trackPanel;
	private AudioTrack audioTrack;
	private long samplePos;
	private AudioStream inStream;
	private AudioStream trackSplit;
	private AudioStream mixerSplit;
	private String trackFileName;
	private boolean isRecording;
	private long lastPeakPos;
	
	public TrackController( String trackFileName, MixerChannel channel, TrackPanel panel ) {
		mixerChannel = channel;
		trackPanel = panel;
		this.trackFileName = trackFileName;
		audioTrack = new AudioTrack();
		inStream = new AudioStream( "TrackPanel " + trackFileName + " inStream ");
		trackSplit = new AudioStream( "TrackPanel " + trackFileName + " trackSplit ");
		mixerSplit = new AudioStream( "TrackPanel " + trackFileName + " mixerSplit ");
	}
	
	public void setPlaybackOrRecord( Asio asio ) {
		isRecording = trackPanel.getRecordState();
		mixerChannel.setIn( inStream );
		if( isRecording ) {
			mixerChannel.setIn( mixerSplit );
			audioTrack.setInput( trackSplit );
			try {
				audioTrack.recordStart(trackFileName + ".raw");
			} catch (IOException e) {
				System.out.println( "Could not record to " + trackFileName );
			}
			inStream.addSyncListener(this);
			asio.connectOutput( mixerChannel.getChannelNumber(), inStream);
		}
		else {
			audioTrack.setInput( null );
			audioTrack.setOutput( inStream );
			try {
				audioTrack.playbackStart(trackFileName + ".raw", false );
			} catch (IOException e) {
				System.out.println( "Could not playback from " + trackFileName );
			}
		}
	}
	
	public boolean isRecording() {
		return isRecording;
	}
	
	public TrackPanel getPanel() {
		return trackPanel;
	}
	
	public AudioTrack getTrack() {
		return audioTrack;
	}
	
	public MixerChannel getMixerChannel() {
		return mixerChannel;
	}
	
	public AudioStream getInpuStream() {
		return inStream;
	}

	@Override
	public void audioSync(long newSamplePos) {
		int deltaPos = ( int )( newSamplePos - samplePos );
		samplePos = newSamplePos;
		if( isRecording ) {
			for( int n = 0; n < deltaPos; n++ ) {
				int sample = inStream.read();
				trackSplit.write( sample );
				mixerSplit.write( sample );
			}
			
			trackSplit.sync( newSamplePos );
			mixerSplit.sync( newSamplePos );
		}
		else {
			inStream.sync(newSamplePos);
			audioTrack.audioSync(newSamplePos);
			mixerChannel.audioSync(newSamplePos);
		}
		
		// VU meter update
		int deltaPeakPos = ( int )(newSamplePos - lastPeakPos);
		if( deltaPeakPos > 2400 ) {
			VUMeter vuIn = trackPanel.getInVUMeter();
			double peakLvl = inStream.peak( deltaPeakPos );
			double vuLevel = vuIn.getMin();
			double logArg = peakLvl / 0x7fffff00;
			if (logArg > 0.001) {
				vuLevel = 20 * Math.log(logArg) + vuIn.getMax();
			}
			vuIn.setVal((int) vuLevel);
			lastPeakPos = newSamplePos;
		}
		
		
	}
}
