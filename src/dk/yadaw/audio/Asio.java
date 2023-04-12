package dk.yadaw.audio;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class holding ASIO interface.
 * 
 * @author tkn
 *
 */
public class Asio {
	
	static {
		String wd = System.getProperty( "user.dir") + "/" + "libasiojni.dll";
		System.out.println( "Loading " + wd );
		System.load( wd );
	}
	
	private boolean isStarted;
	private boolean isStopped;
	private float samplerate;
	private int bufferSize;
	private int outputLatency;
	private int inputLatency;
	private int nofInputs;
	private int nofOutputs;
	private int nofActivatedInputs;
	private int nofActivatedOutputs;
	private Collection<String> drivers;
	private AudioStream[] inputStreams;
	private AudioStream[] outputStreams;
	private Thread bufferSwitchThread;
	private ExecutorService xService;
	
	/**
	 * Construct class and initialize the native library. 
	 */
	public Asio() {
		xService = Executors.newFixedThreadPool(16);
		asioLibInit();
		drivers = new Vector<String>();
		String s = asioGetFirstDriver();
		while( s != null ) {
			drivers.add(s);
			s = asioGetNextDriver();
		}
	}

	/**
	 * List ASIO drivers on system.
	 * @return	Collection of string with driver names.
	 */
	public Collection<String> getDrivers() {
		return drivers;
	}
	
	/**
	 * Loads driver and initialize ASIO system. On success internal info on samplerate etc is fetched from asio subsystem.
	 * @param driverName		ASIO driver name from the collection obtained with getDrivers().
	 * @throws AsioException	If driver cannot be found or initialized.
	 */
	public void openDriver( String driverName ) throws AsioException {
		if( !asioLoadDriver( driverName ) ) {
			throw new AsioException( "Driver \"" + driverName + "\" not found" );
		}
		
		if( !asioInit() ) {
			throw new AsioException( "Asio init of driver \"" + driverName + "\" failed" );
		}
		
		samplerate = ( float )asioGetSamplerate();
		bufferSize = asioGetBufferSize();
		outputLatency = asioGetOutputLatency();
		inputLatency = asioGetInputLatency();
		nofInputs = asioGetAvailableInputs();
		nofOutputs = asioGetAvailableOutputs();
		nofActivatedOutputs = 0;
		nofActivatedInputs = 0;
		
		inputStreams = new AudioStream[nofInputs];
		outputStreams = new AudioStream[nofOutputs];
	}
	

	/**
	 * Set ASIO input stream to a device input channel. 
	 * On ASIO buffer switch the audioSync method of the stream will
	 * be called.
	 * @param ch 		Asio Device input channel
	 * @param inStream	Stream. Input samples will be written to this stream.
	 * @return			true on success.
	 */
	public boolean setInput( int ch, AudioStream inStream ) {
		if( ch < inputStreams.length ) {
			inputStreams[ch] = inStream;
			asioArmInput( ch );
			nofActivatedInputs++;
			return true;
		}
		return false;
	}
	
	/**
	 * Set ASIO output stream to device output channel.
	 * On ASIO buffer switch the audioSync method of the stream will be called.
	 * @param ch
	 * @param outStream
	 * @return
	 */
	public boolean setOutput( int ch, AudioStream outStream ) {
		if( ch < outputStreams.length ) {
			outputStreams[ch] = outStream;
			asioArmOutput( ch );
			nofActivatedOutputs++;
			return true;
		}
		return false;
	}
	
	public float getSamplerate() {
		return samplerate;
	}
	
	public boolean start( String driver ) {		
		try {
			openDriver( driver );
		}
		catch( AsioException ae ) {
			System.out.println( ae );
			return false;
		}
		
		if( asioPrepBuffers() < 0 ) {
			System.out.println( "ASIO Buffer error" );
			return false;
		};
		
		isStarted = false;
		isStopped = false;
		
		bufferSwitchThread = new Thread(){
			@Override
			public void run() {
				int[][] outputBuffer = new int[nofActivatedOutputs][4096];
				int[][] inputBuffer = new int[nofActivatedInputs][4096];
				
				synchronized( this ) {
					do {
						int bufNum = 0;
						for( int n = 0; n < nofOutputs; n++ ) {
							if( outputStreams[n] != null  ) {
								if( outputStreams[n].read( outputBuffer[bufNum++] ) == 0 ) {
									System.out.println( "Bufferswitch Output buffer underrun!" );
								}
							}
						}
						asioSetOutputSamples( outputBuffer );
						
						if( !isStarted ) {
							if( asioStart() < 0 )
							{
								System.out.println( "Error starting ASIO");
								return;
							}
							isStarted = true;
						}

						try {
							wait();
						} catch (InterruptedException e1) {
							System.out.println( "Sample wait interrupted" );
						}
						int nofSamples = asioGetInputSamples( inputBuffer );
						updateInputStreams( inputBuffer, nofSamples );
					} while( !isStopped );
					
					System.out.println( "\ndone");
					asioStop();
				}
			}
		};
		bufferSwitchThread.start();
		return true;
	}
		
	public void stop() {
		isStopped = true;
		try {
			bufferSwitchThread.join();
		} catch (InterruptedException e) {
			System.out.println( "Interrupted waiting on bufferSwitchThread to terminate");
		}
	}
	
	public long getSamplePos() {
		return asioGetSamplePos();
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * Get output latency.
	 * @return	output latency in samples.
	 */
	public int getOutputLatency() {
		return outputLatency;
	}
	
	/**
	 * Get input latency
	 * @return input latency in samples
	 */
	public int getInputLatency() {
		return inputLatency;
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
	
	private void notifySample() {
		this.notify();
	}
	
	private void updateInputStreams( int[][] samples, int nofSamples ) {
		long pos = asioGetSamplePos();
		int bufNum = 0;
		for( AudioStream stream : inputStreams ) {
			if( stream != null ) {
				int[] inBufCopy = new int[nofSamples];
				System.arraycopy( samples[bufNum], 0, inBufCopy, 0, nofSamples );
				if( stream.write( inBufCopy, pos )) {
					xService.submit( () -> stream.sync());
				}
				else {
					System.out.println( "Bufferswitch Input Buffer overrun");
				}
				bufNum++;
			}
		}
	}
	
	private native void asioLibInit();
	private native String asioGetFirstDriver();
	private native String asioGetNextDriver();
	private native boolean asioLoadDriver( String driverName );
	private native boolean asioInit();
	private native double asioGetSamplerate();
	private native long asioGetSamplePos();
	private native int asioGetBufferSize();
	private native int asioGetOutputLatency();
	private native int asioGetInputLatency();
	private native int asioGetAvailableInputs();
	private native int asioGetAvailableOutputs();
	private native void asioClearArmedChannels();
	private native void asioArmInput( int ch );
	private native void asioArmOutput( int ch );
	private native int asioSetOutputSamples( int[][] outputSamples );
	private native int asioGetInputSamples( int[][] inputSamples );
	private native int asioPrepBuffers();
	private native int asioStart();
	private native void asioStop();
	
	
	public static void main( String args[] ) {
		System.out.println( "ASIO test" );
		String theDriver = null;
		Asio as = new Asio();
		Collection<String> drivers = as.getDrivers();
		for( String s : drivers ) {
			if( s.contains( "Focusrite") && s.contains( "USB" ) ) {
				theDriver = s;
				System.out.println( "Using ASIO driver \"" + s + "\"" );
			}
		}
		
		if( theDriver != null ) {
			System.out.println( "Opening driver: " + theDriver );
			try {
				as.openDriver(theDriver);
				System.out.println( "  Number of inputs: " + as.getNofInputs() );
				System.out.println( "  Number of outputs: " + as.getNofOutputs() );
				System.out.println( "  Input latency: " + as.getInputLatency() );
				System.out.println( "  Output latency: " + as.asioGetOutputLatency() );
				System.out.println( "  SampleRate: " + as.getSamplerate() );
				System.out.println( "  Buffer size: " + as.getBufferSize() );
			} catch (AsioException e) {
				e.printStackTrace();
			}			
		}
	}
}
