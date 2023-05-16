package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Representing mixer - the main sound router in the system.
 * @author tkn
 *
 */
public class Mixer implements SyncListener {
	private MixerChannel[] channels;
	private AudioStream sumLeft;
	private AudioStream sumRight;
	private AudioStream[] sumSends;
	private int sampleBufferLength;
	private int[][] sumBuffers;
	
	public Mixer( int numChannels, int numSends, int sampleBufferLength ) {
		channels = new MixerChannel[numChannels];
		sumSends = new AudioStream[numSends];
		this.sampleBufferLength = sampleBufferLength;
		sumBuffers = new int[numChannels * numSends][sampleBufferLength];
		for( int n = 0; n < numChannels; n++ ) {
			channels[n] = new MixerChannel( numSends );
			sumSends[n] = new AudioStream();
		}
	}

	public void setSumLeft( AudioStream left ) {
		sumLeft = left;
		sumLeft.addSyncListener(this);
	}
	
	public void setSumRight( AudioStream right ) {
		sumRight = right;
	}
	
	@Override
	public void audioSync(AudioStream s) {
		for( MixerChannel mch : channels ) {
			mch.audioSync(s);
		}
		
		for (int channel = 0; channel < channels.length; channel++) {
			for (int snd = 0; snd < sumSends.length; snd++) {
				sumBuffers[channel*sumSends.length + snd] = channels[channel].getSend(snd).read().getBuffer();
			}
		}
	}
	
}
