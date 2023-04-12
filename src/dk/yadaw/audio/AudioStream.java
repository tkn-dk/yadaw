package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an audio stream.
 */
public class AudioStream {
	private final int nofBuffers = 4;
	private int[][] streamBuffers;
	private long[] samplePos;
	int wb;
	int rb;
	private Set<SyncListener> syncListeners;
	
	public AudioStream( ) {
		streamBuffers = new int[nofBuffers][];
		samplePos = new long[nofBuffers];
		syncListeners = new HashSet<SyncListener>();
		wb = 0;
		rb = 0;
	}
	
	public long read( int[] samples ) {
		long spos = 0;
		if( rb != wb ) {
			spos = samplePos[rb];
			samples = streamBuffers[rb++];
			if( rb == nofBuffers )
			{
				rb = 0;
			}
		}
		return spos;
	}
	
	public boolean write( int[] samples, long samplePos ) {
		int nwb = wb + 1;
		if( nwb == nofBuffers ) {
			nwb = 0;
		}
		
		if( nwb == rb ) {
			return false;
		}
		
		wb = nwb;
		streamBuffers[wb] = samples;
		return true;
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
