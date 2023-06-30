package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an audio stream.
 */
public class AudioStream {
	private String label;
	private int[] buffer;
	private long samplePos;
	private int wptr;
	private int rptr;
	private int cptr;
	private Set<SyncListener> syncListeners;
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
		syncListeners = new HashSet<SyncListener>();
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
	
	public void addSyncListener( SyncListener sl ) {
		syncListeners.add(sl);
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

		for (SyncListener s : syncListeners) {
			s.audioSync(newSamplePos);
		}
	}
	
	public String toString() {
		return "AudioStream: " + label;
	}
	
}
