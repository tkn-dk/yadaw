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
			wb = nwb;
			streamBuffers[wb].setSamplePos(spos);
			streamBuffers[wb].setBuffer(samples);
			return true;
		}
		return false;
	}
	
	public boolean isFull() {
		int nwb = wb + 1;
		if( nwb == nofBuffers ) {
			nwb = 0;
		}
		
		if( nwb == rb ) {
			return true;
		}
		return false;
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
