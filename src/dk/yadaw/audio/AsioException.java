package dk.yadaw.audio;

/**
 * ASIO related exceptions.
 * @author tkn
 *
 */
public class AsioException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public AsioException( String message ) {
		super( message );
	}
}
