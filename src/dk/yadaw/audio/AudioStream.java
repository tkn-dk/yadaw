package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an audio stream.
 */
public class AudioStream {
	private final int nofBuffers = 4;
	private AudioStreamBuffer[] streamBuffers;
	int wb;
	int rb;
	int nextWb; 
	private Set<SyncListener> syncListeners;
	
	public AudioStream( ) {
		streamBuffers = new AudioStreamBuffer[nofBuffers];
		for( int n = 0; n < streamBuffers.length; n++ ) {
			streamBuffers[n] = new AudioStreamBuffer();
		}
		syncListeners = new HashSet<SyncListener>();
		wb = 0;
		rb = 0;
	}
	
	public AudioStreamBuffer read() {
		AudioStreamBuffer abuf = null;
		if( rb != wb ) {
			abuf = streamBuffers[rb++];
			if( rb == nofBuffers )
			{
				rb = 0;
			}
		}
		return abuf;
	}
	
	public int availableNextRead() {
		if( rb != wb ) {
			return streamBuffers[rb].getBuffer().length;
		}
		return 0;
	}
	
	public boolean write( int[] samples, long spos ) {
		if( !isFull() ) {			
			streamBuffers[wb].setSamplePos(spos);
			streamBuffers[wb].setBuffer(samples);
			wb = nextWb;
			return true;
		}
		return false;
	}
	
	public boolean isFull() {
		nextWb = wb + 1;
		if( nextWb == nofBuffers ) {
			nextWb = 0;
		}
		
		if( nextWb == rb ) {
			return true;
		}
		return false;
	}
	
	public boolean isEmpty() {
		return rb == wb;
	}
	
	public void addSyncListener( SyncListener sl ) {
		syncListeners.add(sl);
	}
	
	public void sync( ) {
		for( SyncListener s : syncListeners ) {
			s.audioSync( this );
		}
	}
	
}
