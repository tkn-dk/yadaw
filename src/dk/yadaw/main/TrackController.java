package dk.yadaw.main;

import dk.yadaw.audio.AudioTrack;
import dk.yadaw.audio.Mixer;
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
	private AudioTrack fileTrack;
	private String trackFileName;
	private boolean isRecording;
	
	public TrackController( String trackFileName, MixerChannel channel, TrackPanel panel ) {
		mixerChannel = channel;
		trackPanel = panel;
		this.trackFileName = trackFileName;
		mixerChannel.getIn().addSyncListener(this);
		audioTrack = new AudioTrack();
	}
	
	public void setRecording() {
		isRecording = true;
		audioTrack.setInput( mixerChannel.getIn());
		//TODO: Find a way to still feed the stream to mixer channel - the line above cuts of 
	}
	
	public void setPlayback() {
		isRecording = false;
		audioTrack.setOutput( mixerChannel.getIn());
	}

	@Override
	public void audioSync(long newSamplePos) {
		int deltaPos = ( int )( newSamplePos - samplePos );
		samplePos = newSamplePos;
		
		VUMeter vuIn = trackPanel.getInVUMeter();
		int peakLvl = mixerChannel.getIn().peak( deltaPos );
		double vuLevel = vuIn.getMin();
		if( peakLvl > 0 ) {
			vuLevel = 20 * Math.log( peakLvl / 0xffffff00 ) + vuIn.getMax();
		}
		vuIn.setVal( ( int )vuLevel );
	}
}
