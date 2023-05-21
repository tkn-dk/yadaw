
package dk.yadaw.audio;

/**
 * Interface to implement to capture sync events from asio.
 * The events are send whenever a new buffer is processed on the asio level.
 * {@link AudioStream} and {@link AudioProducer} 
 * @author tkn
 *
 */
public interface SyncListener {
	public void audioSync( long samplePos );
}
