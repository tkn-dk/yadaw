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
	private long samplePos;
	
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
		addChannel( new MixerChannel( label, numSends ));
	}
	
	public void addChannel( MixerChannel mxc ) {
		channels.add( mxc );		
	}
	
	public MixerChannel getChannel( String label ) {
		for( MixerChannel mch : channels ) {
			if( mch.getLabel().equals(label)) {
				return mch;
			}
		}
		return null;
	}
	
	public AudioStream getMasterLeft() {
		return sumLeft;
	}
	
	public AudioStream getMasterRight() {
		return sumRight;
	}
	
	@Override
	public void audioSync( long newSamplePos ) {
		for( MixerChannel mch : channels ) {
			mch.audioSync( samplePos );
		}
		
		int deltaPos = ( int )( newSamplePos - samplePos );
		samplePos = newSamplePos;
		sumMasters( 4 * deltaPos );	
	}
	
	private void sumMasters( int processWish ) {
		int minAvailable = Integer.MAX_VALUE;
		
		// We need to find the channel with least amount of samples to process
		// as mixer channels can be fed from a file or ASIO input
		for( MixerChannel est : channels ) {
			// Enough to only check one of the master channels as they are fed from same input
			int available = est.getMasterLeft().available();
			if( available < minAvailable ) {
				minAvailable = available;
			}
		}
		
		int toTransfer = Math.min( Math.min( minAvailable, sumLeft.free() ), processWish );
		for( int sample = 0; sample < toTransfer; sample++ ) {
			int left = 0;
			int right = 0;
			for( MixerChannel mc : channels ) {
				left += ( int )(( ( long )mc.getMasterLeft().read() * masterGainLeft ) >> 32 );
				right += ( int ) (( ( long )mc.getMasterRight().read() * masterGainRight ) >> 32 );
			}
			sumLeft.write( left );
			sumRight.write( right );
		}
	}
	
}
