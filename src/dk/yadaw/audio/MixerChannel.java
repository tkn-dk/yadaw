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
	private int channelNumber;
	private AudioStream in;
	private AudioStream[] sends;
	private AudioStream masterOutLeft;
	private AudioStream masterOutRight;
	private int[] sendGains;
	private int masterLeftGain;
	private int masterRightGain;
	private int masterGain;
	private int pan;
	private int maxPan;
	private long samplePos;
	private int[] inBuffer;

	/**
	 * Mixer channel constructor
	 * @param label Label for mixertrack - used to construct track file name
	 * when recording.
	 */
	public MixerChannel( String label, int channelNumber, int nofSends ) {
		sends = new AudioStream[nofSends];
		sendGains = new int[nofSends];
		inBuffer = new int[2048];
		this.channelNumber = channelNumber;
		
		for( int n = 0; n < nofSends; n++ ) {
			sends[n] = new AudioStream();
			sendGains[n] = 0;
		}
		
		this.label = label;
		masterOutLeft = new AudioStream( label + "out left" );
		masterOutRight = new AudioStream( label + "out right" );
		masterGain = 0x7fff0000;	
		pan = 0;
		maxPan = 20;
		setPannedGain();
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
	
	public int getChannelNumber() {
		return channelNumber;
	}
	
	public void setIn( AudioStream in ) {
		System.out.println( "MixerChannel " + label + " set " + in + " as input");
		this.in = in;
	}
	
	public void setMasterGain( int gain ) {
		masterGain = gain;
		setPannedGain();
	}
	
	public void setPan( int value, int max ) {
		maxPan = Math.abs(max);
		pan = value;
		setPannedGain();
	}

	private void setPannedGain() {
		if (pan > 0) {
			masterLeftGain = (int) (((long) masterGain * ( maxPan - pan )) / (maxPan));
			masterRightGain = masterGain;
		} else if (pan < 0) {
			int cpan = Math.abs(pan);
			masterLeftGain = masterGain;
			masterRightGain = (int) (((long) masterGain * (maxPan - cpan)) / maxPan);
		} else {
			masterRightGain = masterGain;
			masterLeftGain = masterGain;
		}
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
	public void audioSync( long newSamplePos ) {
		//System.out.println( "MixerChannel " + label + " pos: " + newSamplePos + " avail: " + in.available());
		masterOutLeft.sync(newSamplePos);
		masterOutRight.sync(newSamplePos);
		
		int deltaPos = (int) (newSamplePos - samplePos);
		if (deltaPos > 0) {
			samplePos = newSamplePos;
			processInput(4 * deltaPos);
		}
	}
	
	public void processInput( int processWish ) {
		int toTransfer = Math.min( in.available(), processWish );
		int transferredSamples = 0;

		while( transferredSamples < toTransfer ) {
			inBuffer[transferredSamples++] = in.read();
		}
		
		for( int n = 0; n < sends.length; n++ ) {
			//routeSend( n, toTransfer );
		}
		
		routeMasters( toTransfer );
	}
	
	private void routeSend( int send, int toProcess ) {
		for( int n = 0; n < toProcess; n++ ) {
			int sample = ( int )( (( long )inBuffer[n] * sendGains[send] ) >> 32 );
			sends[send].write( sample );
		}
	}
	
	private void routeMasters( int toTransfer ) {
		for( int n = 0; n < toTransfer; n++ ) {
			int leftSample =  ( int )( (( long )inBuffer[n] * masterLeftGain ) >> 32 );
			int rightSample = ( int )( (( long )inBuffer[n] * masterRightGain ) >> 32 );
			masterOutLeft.write( leftSample );
			masterOutRight.write( rightSample );
		}		
	}

}
