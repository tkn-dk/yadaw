package dk.yadaw.audio;

import java.util.ArrayList;

/**
 * Representing mixer - the main sound router in the system.
 * @author tkn
 *
 */
public class Mixer implements SyncListener {
	private ArrayList<MixerChannel> channels;
	private AudioStream sumLeft;
	private AudioStream sumRight;
	private int bufferLength;
	private int masterGainLeft;
	private int masterGainRight;
	
	public Mixer( int bufferLength ) {
		channels = new ArrayList<MixerChannel>();
		this.bufferLength = bufferLength;
	}

	public void setMaster( AudioStream left, AudioStream right ) {
		sumLeft = left;
		sumRight = right;
		sumLeft.addSyncListener(this);
	}
	
	public void setMasterGain( int left, int right ) {
		masterGainLeft = left;
		masterGainRight = right;
	}
	
	public void addChannel( String label,  int numSends ) {
		channels.add( new MixerChannel( label, numSends ));
	}
	
	public MixerChannel getChannel( String label ) {
		for( MixerChannel mch : channels ) {
			if( mch.getLabel().equals(label)) {
				return mch;
			}
		}
		return null;
	}
	
	@Override
	public void audioSync( long samplePos ) {
		for( MixerChannel mch : channels ) {
			mch.audioSync( samplePos );
		}
		
		sumMasters();	
	}
	
	private void sumMasters() {
	}
	
}
