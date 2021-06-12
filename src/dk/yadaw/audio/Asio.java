package dk.yadaw.audio;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

/**
 * Class holding ASIO interface.
 * 
 * @author tkn
 *
 */
public class Asio {
	private boolean isStarted;
	private double samplerate;
	private int bufferSize;
	private int latency;
	private int nofInputs;
	private int nofOutputs;
	private int nofActivatedInputs;
	private int nofActivatedOutputs;
	
	/**
	 * Construct class and initialize the native library. 
	 */
	public Asio() {
		asioLibInit();
	}
	
	/**
	 * List ASIO drivers on system.
	 * @return	Collection of string with driver names.
	 */
	public Collection<String> getDrivers() {
		Collection<String> drivers = new Vector<String>();
		String s = asioGetFirstDriver();
		while( s.length() > 0 ) {
			drivers.add(s);
			s = asioGetNextDriver();
		}
		return drivers;
	}
	
	/**
	 * Loads driver and initialize ASIO system. On success internal info on samplerate etc is fetched from asio subsystem.
	 * @param driverName		ASIO driver name from the collection obtained with getDrivers().
	 * @throws AsioException	If driver cannot be found or initialized.
	 */
	public void open( String driverName ) throws AsioException {
		if( !asioLoadDriver( driverName ) ) {
			throw new AsioException( "Driver not found" );
		}
		
		if( !asioInit() ) {
			throw new AsioException( "Asio init failed" );
		}
		
		samplerate = asioGetSamplerate();
		bufferSize = asioGetBufferSize();
		latency = asioGetLatency();
		nofInputs = asioGetAvailableInputs();
		nofOutputs = asioGetAvailableOutputs();
		nofActivatedOutputs = 0;
		nofActivatedInputs = 0;
	}
	
	public double getSamplerate() {
		return samplerate;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * Get system latency.
	 * @return	latency in samples.
	 */
	public int getLatency() {
		return latency;
	}
	
	public int getNofInputs() {
		return nofInputs;
	}
	
	public int getNofOutputs() {
		return nofOutputs;
	}
	
	public int getNofActivatedInputs() {
		return nofActivatedInputs;
	}
	
	public int getNofActivatedOutputs() {
		return nofActivatedOutputs;
	}
	
	/**
	 * Activates an input so its input samples will be added to the buffer. 
	 * @param ch	Channel num - zero based.
	 */
	public void activateInput( int ch ) {
		asioActivateInput( ch );
	}
	
	/**
	 * Activates an output, data must be added to outputbuffer.
	 * @param ch
	 */
	public void activateOutput( int ch ) {
		asioActivateOutput( ch );
	}
	
	public void deactivateInputs() {
		asioClearUsedInputs();
	}
	
	public void deactivateOutputs() {
		asioClearUsedOutputs();
	}
	
	/**
	 * Exchange input and output buffers with ASIO system.
	 * @param outputBuffer	Linear buffer of sample buffers for output - left adjusted signed integers. Format is { s_ch_n1[bufferSize], s_ch_n2[bufferSize}+] ... } 
	 * @return inputBuffer	Linear buffer of sample buffers for input. Same format as output buffer. 
	 */
	public int[] exchangeBuffers( int[] outputBuffer ) {
		if( !isStarted ) {
			asioStart();
			isStarted = true;
		}
		
		synchronized( this ) {
			try {
				wait();
				return asioExchangeBuffers( outputBuffer );
			}
			catch( InterruptedException e ) {
				asioStop();
				isStarted = false;
			}
		}
		return null;
	}
	
	private native void asioLibInit();
	private native String asioGetFirstDriver();
	private native String asioGetNextDriver();
	private native boolean asioLoadDriver( String driverName );
	private native boolean asioInit();
	private native double asioGetSamplerate();
	private native int asioGetBufferSize();
	private native int asioGetLatency();
	private native int asioGetAvailableInputs();
	private native int asioGetAvailableOutputs();
	private native void asioClearUsedInputs();
	private native void asioClearUsedOutputs();
	private native void asioActivateInput( int ch );
	private native void asioActivateOutput( int ch );
	private native void asioPrepBuffers();
	private native int[] asioExchangeBuffers( int[] outputSamples );
	private native void asioStart();
	private native void asioStop();
}
