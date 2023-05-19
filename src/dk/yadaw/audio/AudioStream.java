package dk.yadaw.audio;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an audio stream.
 */
public class AudioStream {
	private final int nofBuffers = 8;
	private AudioStreamBuffer[] streamBuffers;
	int wb;
	int rb;
	int nextWb; 
	int peak;
	private Set<SyncListener> syncListeners;
	private Set<PeakListener> peakListeners;
	
	public AudioStream( ) {
		streamBuffers = new AudioStreamBuffer[nofBuffers];
		for( int n = 0; n < streamBuffers.length; n++ ) {
			streamBuffers[n] = new AudioStreamBuffer();
		}
		syncListeners = new HashSet<SyncListener>();
		peakListeners = new HashSet<PeakListener>();
		wb = 0;
		rb = 0;
	}
	
	public AudioStreamBuffer read() {
		AudioStreamBuffer abuf = null;
		if( rb != wb ) {
			if( peakListeners.size() > 0 ) {
				peak = calculateFrontPeak();
				for( PeakListener l : peakListeners ) {
					// TODO: Investigate if this should be handed over to ExecutorService
					// to not delay return of read().
					l.peakUpdate(peak);
				}
			}
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
	
	public int getLastReadPeak() {
		return peak;
	}
	
	/**
	 * Calculate peak value from next buffer leaving
	 * the audiostream
	 * @return
	 */
	private int calculateFrontPeak() {
		int peak = 0;
		int[] frontBuffer = streamBuffers[rb].getBuffer();
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		for (int n = 0; n < frontBuffer.length; n++) {
			if (frontBuffer[n] > max) {
				max = frontBuffer[n];
			}

			if (frontBuffer[n] < min) {
				min = frontBuffer[n];
			}
		}
		peak = Math.max(Math.abs(max), Math.abs(min));
		return peak;
	}
	
	public void addPeakListener( PeakListener pl ) {
		peakListeners.add( pl );
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
