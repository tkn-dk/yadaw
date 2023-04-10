package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an audio stream.
 */
public class AudioStream implements SyncListener {
	private final int nofBuffers = 4;
	private int[][] streamBuffers;
	int wb;
	int rb;
	private Set<SyncListener> syncListeners;
	
	public AudioStream( ) {
		streamBuffers = new int[nofBuffers][];
		syncListeners = new HashSet<SyncListener>();
		wb = 0;
		rb = 0;
	}
	
	public boolean read( int[] samples ) {
		if( rb == wb ) {
			return false;
		}
		samples = streamBuffers[rb++];
		if( rb == nofBuffers )
		{
			rb = 0;
		}
		return true;
	}
	
	public boolean write( int[] samples ) {
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
	
	public void audioSync( float timeCode, long samplePos, int peakVal ) {
		for( SyncListener s : syncListeners ) {
			s.audioSync(timeCode, samplePos, peakVal);
		}
	}
	
}
