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
	private int masterGainLeft;
	private int masterGainRight;
	private long samplePos;
	
	public Mixer( ) {
		channels = new ArrayList<MixerChannel>();
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
	
	public void addChannel( String label, int channelNumber, int numSends ) {
		addChannel( new MixerChannel( label, channelNumber, numSends ));
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
		int processWish = 4 * deltaPos;	
		int minAvailable = Integer.MAX_VALUE;
		
		// We need to find the channel with least amount of samples to process
		// as mixer channels can be fed from a file or ASIO input
		for( MixerChannel est : channels ) {
			// Enough to sync on master left, as the mixechannel is only listening on that.
			est.getMasterLeft().sync( newSamplePos );
			
			// Enough to only check one of the master channels as they are fed from same input
			int available = est.getMasterLeft().available();
			if( available < minAvailable ) {
				minAvailable = available;
			}
		}
		
		int toTransfer = Math.min( Math.min( minAvailable, sumLeft.free() ), processWish );
		if (toTransfer == 0) {
			System.out.println("Mixer Master no data:");
			System.out.println("  toTransfer: " + toTransfer + ", minAvailable: " + minAvailable + ", sumLeft.free(): "
					+ sumLeft.free() + ", minAvailable: " + minAvailable + ", processWish: " + processWish);
		}
		
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
