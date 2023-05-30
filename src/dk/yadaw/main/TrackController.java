package dk.yadaw.main;

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
	private long samplePos;
	
	public TrackController( MixerChannel channel, TrackPanel panel ) {
		this.mixerChannel = channel;
		this.trackPanel = panel;
		this.mixerChannel.getIn().addSyncListener(this);
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
