package dk.yadaw.audio;

/**
 * Representing one channel of a mixer with input from either a track
 * file (playback) or Sound interface.
 * It has two sends, pan and volume regulation.
 * @author tkn
 *
 */
public class MixerChannel implements SyncListener {
	private String label;
	private AudioStream in;
	private AudioStream[] sends;
	private AudioStream masterOutLeft;
	private AudioStream masterOutRight;
	private int[] sendGains;
	private int masterLeftGain;
	private int masterRightGain;

	/**
	 * Mixer channel constructor
	 * @param label Label for mixertrack - used to construct track file name
	 * when recording.
	 */
	public MixerChannel( int nofSends ) {
		sends = new AudioStream[nofSends];
		sendGains = new int[nofSends];
		
		for( int n = 0; n < nofSends; n++ ) {
			sends[n] = new AudioStream();
			sendGains[n] = 0;
		}
		
		masterOutLeft = new AudioStream();
		masterOutRight = new AudioStream();
		// All synchronization is done on masterOutLeft
		masterOutLeft.addSyncListener(this);
	}

	public AudioStream getSend( int num ) {
		if( num < sends.length ) {
			return sends[num];
		}
		return null;
	}
	
	public AudioStream getMasterLeft() {
		return masterOutLeft;
	}
	
	public AudioStream getMasterRight() {
		return masterOutRight;
	}
	
	public AudioStream getIn() {
		return in;
	}
	
	public void setIn( AudioStream in ) {
		this.in = in;
	}
	
	public void setMasterLeftGain( int gain ) {
		masterLeftGain = gain;
	}
	
	public void setMasterRightGain( int gain ) {
		masterRightGain = gain;
	}
	
	public void setSendGain( int num, int gain ) {
		if( num < sends.length ) {
			sendGains[num] = gain;
		}
	}

	public int getMasterLeftGain() {
		return masterLeftGain;
	}
	
	public int getMasterRightGain() {
		return masterRightGain;
	}

	public int getSendGain( int num ) {
		int g = 0;
		if( num < sends.length ) {
			g =  sendGains[num];
		}
		return g;
	}
	
	public void setLabel( String label ) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public void audioSync(AudioStream s) {
		if( s == masterOutLeft ) {
			if ( !masterOutLeft.isFull() ) {
				AudioStreamBuffer inbuf = in.read();
				if (inbuf != null) {
					routeSends( inbuf );
					routeMasters( inbuf );
				}
			}
		}
	}
	
	private void routeSends( AudioStreamBuffer inputBuf ) {
		int[] inBuffer = inputBuf.getBuffer();
		for( int snd = 0; snd < sends.length; snd++ ) {
			int[] sendBuffer = new int[inBuffer.length];
			for( int sample = 0; sample < inBuffer.length; sample++ ) {
				long ss = ( long )inBuffer[sample] * sendGains[snd];
				sendBuffer[sample] = ( int )( ss >> 32 );
			}
			sends[snd].write(sendBuffer, inputBuf.getSamplePos() );
		}
	}
	
	private void routeMasters( AudioStreamBuffer audioBuf ) {
		int[] inBuffer = audioBuf.getBuffer();
		int[] masterLeftBuffer = new int[inBuffer.length];
		int[] masterRightBuffer = new int[inBuffer.length];
		for( int sample = 0; sample < inBuffer.length; sample++ ) {
			long sl = ( long )inBuffer[sample] * masterLeftGain;
			long sr = ( long )inBuffer[sample] * masterRightGain;
			masterLeftBuffer[sample] = ( int )( sl >> 32 );
			masterRightBuffer[sample] = ( int )(sr >> 32 );			
		}
		masterOutLeft.write( masterLeftBuffer, audioBuf.getSamplePos() );
		masterOutRight.write( masterRightBuffer, audioBuf.getSamplePos() );		
	}

}
