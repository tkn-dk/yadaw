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
	private long samplePos;
	private int[] inBuffer;

	/**
	 * Mixer channel constructor
	 * @param label Label for mixertrack - used to construct track file name
	 * when recording.
	 */
	public MixerChannel( String label, int nofSends ) {
		sends = new AudioStream[nofSends];
		sendGains = new int[nofSends];
		inBuffer = new int[2048];
		
		for( int n = 0; n < nofSends; n++ ) {
			sends[n] = new AudioStream();
			sendGains[n] = 0;
		}
		
		this.label = label;
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
	public void audioSync( long samplePos ) {
		int deltaPos = (int) (samplePos - this.samplePos);
		if (deltaPos > 0) {
			this.samplePos = samplePos;
			processInput(4 * deltaPos);
		}
	}
	
	public void processInput( int processWish ) {
		int toTransfer = Math.min( Math.min( in.available(), 4 * processWish ), sends[0].available() );
		int transferredSamples = 0;
		
		while( transferredSamples < toTransfer ) {
			inBuffer[transferredSamples++] = in.read();
		}
		
		for( int n = 0; n < sends.length; n++ ) {
			routeSend( n, toTransfer );
		}
		
		routeMasters( toTransfer );
	}
	
	private void routeSend( int send, int toProcess ) {
		for( int n = 0; n < toProcess; n++ ) {
			int sample = ( int )( (( long )inBuffer[n] * sendGains[send] ) >> 32 );
			sends[send].write( sample );
		}
	}
	
	private void routeMasters( int toProcess ) {
		for( int n = 0; n < toProcess; n++ ) {
			int leftSample = ( int )( (( long )inBuffer[n] * masterLeftGain ) >> 32 );
			int rightSample = ( int )( (( long )inBuffer[n] * masterRightGain ) >> 32 );
			masterOutLeft.write( leftSample );
			masterOutRight.write( rightSample );
		}		
	}

}
