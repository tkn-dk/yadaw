package dk.yadaw.audio;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

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
	private double samplerate;
	private int bufferSize;
	private int outputLatency;
	private int inputLatency;
	private int nofInputs;
	private int nofOutputs;
	private int nofActivatedInputs;
	private int nofActivatedOutputs;
	private Collection<String> drivers;
	
	/**
	 * Construct class and initialize the native library. 
	 */
	public Asio() {
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
	public void open( String driverName ) throws AsioException {
		if( !asioLoadDriver( driverName ) ) {
			throw new AsioException( "Driver \"" + driverName + "\" not found" );
		}
		
		if( !asioInit() ) {
			throw new AsioException( "Asio init of driver \"" + driverName + "\" failed" );
		}
		
		samplerate = asioGetSamplerate();
		bufferSize = asioGetBufferSize();
		outputLatency = asioGetOutputLatency();
		inputLatency = asioGetInputLatency();
		nofInputs = asioGetAvailableInputs();
		nofOutputs = asioGetAvailableOutputs();
		nofActivatedOutputs = 0;
		nofActivatedInputs = 0;
	}
	
	public double getSamplerate() {
		return samplerate;
	}
	
	public void recordTrack( int channel, String driver, String filename ) {
		
		try {
			open( driver );
		}
		catch( AsioException ae ) {
			System.out.println( ae );
			return;
		}
		
		FileOutputStream file;
		try {
			file = new FileOutputStream( filename );
		} catch (FileNotFoundException e) {
			System.out.println( "File path \"" + filename + "\" not found" );
			return;
		}
		
		BufferedOutputStream sampleStream = new BufferedOutputStream( file );
		int[] secBuffer = new int[2000];
		int sampleNum = 0;
		clearArmedChannels();
		armInput( channel );
		asioPrepBuffers();
		
		int[] outputBuffer = new int[bufferSize];
		long samplePos;
		byte[] bSamples = new byte[2000000];
		int bsix = 0;
		do {
			int[] inputBuffer = exchangeBuffers( outputBuffer );
			samplePos = asioGetSamplePos();
			for( int n = 0; n < bufferSize; n++ ) {
				int s = inputBuffer[n];
				bSamples[bsix++] = ( byte )(s >> 24);
				bSamples[bsix++] = ( byte )(s >> 16);
				bSamples[bsix++] = ( byte )(s >> 8);
				
				if( sampleNum < secBuffer.length ) {
					secBuffer[sampleNum++] = s;
				}
			}
		} while( isStarted && samplePos < 480000 );
		
		try {
			System.out.println( "Writing samples ...");
			sampleStream.write( bSamples, 0, bsix );
		} catch (IOException e) {
			System.out.println( "Error writing to file \"" + filename + "\"" );
		}
		
		System.out.println( "\ndone");
		asioStop();
		
		try {
			sampleStream.close();
		} catch (IOException e) {
			System.out.println( "Error closing file \"" + filename + "\"" );
		}
		
		// Dump samples for debug
		try {
			FileWriter sampFile = new FileWriter( "samples.csv" );
			BufferedWriter sampWriter = new BufferedWriter( sampFile );
			
			for( int s : secBuffer )
			{
				sampWriter.write( s + ";\n");
			}
			sampWriter.close();
		} catch (IOException e) {
			System.out.println( "Error opening sample file");
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
	
	/**
	 * Activates an input so its input samples will be added to the buffer. 
	 * @param ch	Channel num - zero based.
	 */
	public void armInput( int ch ) {
		asioArmInput( ch );
	}
	
	/**
	 * Activates an output, data must be added to outputbuffer.
	 * @param ch
	 */
	public void armOutput( int ch ) {
		asioArmOutput( ch );
	}
	
	public void clearArmedChannels() {
		asioClearArmedChannels();
	}
	
	/**
	 * Exchange input and output buffers with ASIO system.
	 * @param outputBuffer	Linear buffer of sample buffers for output - left adjusted signed integers. Format is { s_ch_n1[bufferSize], s_ch_n2[bufferSize}+] ... } 
	 * @return inputBuffer	Linear buffer of sample buffers for input. Same format as output buffer. Note that the returned array reference will be
	 * 						overwritten on next call to exchangeBuffers - so remember to copy data into Sample stream.
	 */
	public int[] exchangeBuffers( int[] outputBuffer ) {
		synchronized( this ) {
			try {
				asioSetOutputSamples( outputBuffer );
				if( !isStarted ) {
					asioStart();
					isStarted = true;
				}				
				wait();
				return asioGetInputSamples();
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
	private native long asioGetSamplePos();
	private native int asioGetBufferSize();
	private native int asioGetOutputLatency();
	private native int asioGetInputLatency();
	private native int asioGetAvailableInputs();
	private native int asioGetAvailableOutputs();
	private native void asioClearArmedChannels();
	private native void asioArmInput( int ch );
	private native void asioArmOutput( int ch );
	private native void asioPrepBuffers();
	private native void asioSetOutputSamples( int[] outputSamples );
	private native int[] asioGetInputSamples();
	private native int asioStart();
	private native void asioStop();
	
	private void notifySample() {
		this.notify();
	}
	
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
				as.open(theDriver);
				System.out.println( "  Number of inputs: " + as.getNofInputs() );
				System.out.println( "  Number of outputs: " + as.getNofOutputs() );
				System.out.println( "  Input latency: " + as.getInputLatency() );
				System.out.println( "  Output latency: " + as.asioGetOutputLatency() );
				System.out.println( "  SampleRate: " + as.getSamplerate() );
				System.out.println( "  Buffer size: " + as.getBufferSize() );

				if( args.length > 1 ) {
					if( args[0].equalsIgnoreCase( "play" )) {
						System.out.println( "playback: " + args[1] );
					}
					else if( args[0].equalsIgnoreCase( "record" )) {
						System.out.println( "record: " + args[1] );
						as.recordTrack(0, theDriver, args[1] );
					}				
				}
			} catch (AsioException e) {
				e.printStackTrace();
			}			
		}
	}
}
