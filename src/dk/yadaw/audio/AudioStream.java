package dk.yadaw.audio;

/**
 * @author tkn@korsdal.dk
 * Represents an audio stream as a circular buffer with a commit pointer (cptr).
 * The commit pointer is used to keep the samples in the buffer until they are
 * released by calling AudioStream.sync() with the current sample positions.
 * This enables calculation of peak value for VU meter readout in sync with the stream. 
 */
public class AudioStream {
	private String label;
	private int[] buffer;
	private long samplePos;
	private int wptr;
	private int rptr;
	private int cptr;
	private boolean writeTransferCompleted;
	
	public AudioStream( String label ) {
		this( 16384 );
		this.label = label;
	}
	
	public AudioStream( ) {
		this( 16384 );
	}

	public AudioStream( int bufferSize ) {
		buffer = new int[bufferSize];
		label = "none";
	}
	
	public int read() {
		int rval = 0 ;
		if( rptr != wptr ) {
			rval = buffer[rptr++];
			if( rptr == buffer.length )
			{
				rptr = 0;
			}
		}
		return rval;
	}
	
	public boolean write( int sample ) {
		int nextWb = wptr + 1;
		if( nextWb == buffer.length ) {
			nextWb = 0;
		}
		
		if( nextWb != cptr ) {
			buffer[wptr] = sample;
			wptr = nextWb;
			return true;
		}
		System.out.println( label + " write overflow");
		return false;
	}
	
	public int available() {
		return ( wptr >= rptr ) ? ( wptr - rptr ) : ( buffer.length - rptr + wptr ); 
	}
	
	public int free() {
		return (( cptr > wptr ) ? ( cptr - wptr ) : ( buffer.length - wptr + cptr )) - 1;
	}
	
	public boolean isFull() {
		int nextWb = wptr + 1;
		if( nextWb == buffer.length ) {
			nextWb = 0;
		}
		
		if( nextWb == cptr ) {
			return true;
		}
		return false;
	}
	
	public boolean isEmpty() {
		return rptr == wptr;
	}
	
	public boolean getWriteTransferCompleted() {
		return writeTransferCompleted;
	}
	
	public void setWriteTransferCompleted( boolean state ) {
		writeTransferCompleted = state;
	}
	
	public int peak( int nofSamples ) {
		int peak = 0;
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		int prd = cptr;
		int n = 0;
		
		while( n < nofSamples && prd != wptr ) {
			if (buffer[prd] > max) {
				max = buffer[prd];
			}

			if (buffer[prd] < min) {
				min = buffer[prd];
			}
			
			if( ++prd == buffer.length ) {
				prd = 0;
			}
			
			n++;
		}
		peak = Math.max(Math.abs(max), Math.abs(min));
		return peak;
	}
	
	public void sync( long newSamplePos ) {
		long releasedSamples = newSamplePos - samplePos;
		int committedSamples = ( wptr >= cptr ) ? wptr - cptr : buffer.length - cptr + wptr;
		int commitReadDistance = ( rptr >= cptr ) ? rptr - cptr : buffer.length - cptr + rptr; 
		
		if (committedSamples > releasedSamples) {
			if (releasedSamples < commitReadDistance) {
				cptr = (int) ((cptr + releasedSamples) % buffer.length);
				samplePos = newSamplePos;
			}
		} else {
			cptr = wptr;
			samplePos = newSamplePos;
		}
	}
	
	public String toString() {
		return "AudioStream: " + label;
	}
	
}
