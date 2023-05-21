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
		int leftInputs[][] = new int[channels.size()][];
		int rightInputs[][] = new int[channels.size()][];
		int leftOutput[] = new int[bufferLength];
		int rightOutput[] = new int[bufferLength];
		
		for( int ch = 0; ch < channels.size(); ch++ ) {
			lBuf = channels.get(ch).getMasterLeft().read();
			leftInputs[ch] = lBuf.getBuffer();
			rBuf = channels.get(ch).getMasterRight().read();
			rightInputs[ch] = rBuf.getBuffer();
		}
		
		for( int sample = 0; sample < bufferLength; sample++ ) {
			leftOutput[sample] = 0;
			rightOutput[sample] = 0;
			for( int ch = 0; ch < channels.size(); ch++ ) {
				leftOutput[sample] += ( ( long )leftInputs[ch][sample] * masterGainLeft ) >> 32;
				rightOutput[sample] += ( ( long )rightInputs[ch][sample] * masterGainRight ) >> 32;
			}
		}
		
		sumLeft.write( leftOutput, lBuf.getSamplePos() );
		sumRight.write(rightOutput, rBuf.getSamplePos() );
	}
	
}
