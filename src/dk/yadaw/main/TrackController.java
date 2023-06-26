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
				audioTrack.playbackStart(trackFileName + ".raw" );
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
		System.out.println( "TrackController sync " + newSamplePos );
		int deltaPos = ( int )( newSamplePos - samplePos );
		samplePos = newSamplePos;
		
		VUMeter vuIn = trackPanel.getInVUMeter();
		int peakLvl = inStream.peak( deltaPos );
		double vuLevel = vuIn.getMin();
		if( peakLvl > 0 ) {
			vuLevel = 20 * Math.log( peakLvl / 0xffffff00 ) + vuIn.getMax();
		}
		vuIn.setVal( ( int )vuLevel );
		
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
			
		}
	}
}
