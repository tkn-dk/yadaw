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
	private Object outputLock;
	
	public Mixer( Object outputLock ) {
		channels = new ArrayList<MixerChannel>();
		masterGainLeft = 0x7fff0000;
		masterGainRight = 0x7fff0000;
		this.outputLock = outputLock;
	}

	public void setMaster( AudioStream left, AudioStream right ) {
		sumLeft = left;
		sumRight = right;
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
		sumLeft.sync(newSamplePos);
		sumRight.sync(newSamplePos);
		
		int deltaPos = ( int )( newSamplePos - samplePos );
		samplePos = newSamplePos;
		int processWish = 4 * deltaPos;	
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
		
		int toTransfer = Math.min(Math.min(minAvailable, sumLeft.free()), processWish);
		
		synchronized (outputLock) {
			for (int sample = 0; sample < toTransfer; sample++) {
				long left = 0;
				long right = 0;
				for (MixerChannel mc : channels) {
					left += mc.getMasterLeft().read();
					right += mc.getMasterRight().read();
				}
				sumLeft.write((int) ((left * masterGainLeft) >> 32));
				sumRight.write((int) ((right * masterGainRight) >> 32));
			}
			sumLeft.setWriteTransferCompleted(true);
			sumRight.setWriteTransferCompleted(true);
			outputLock.notify();
		}
	}
	
}
