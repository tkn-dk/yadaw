package dk.yadaw.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Represents and audio track having both producer and consumer capabilities.
 * @author tkn
 *
 */
public class AudioTrack implements SyncListener {
	private AudioStream in;
	private AudioStream out;
	private BufferedOutputStream fileOutStream;
	private BufferedInputStream fileInStream;
	private byte[] fileBytes;
	
	/**
	 * Constructor
	 * @param name Name of track
	 * @param filePath Path to store track files
	 */
	public AudioTrack() {
		fileBytes = new byte[3];
	}

	
	@SuppressWarnings("unused")
	@Override
	public void audioSync(AudioStream s) {
		if (s == in && fileOutStream != null) {
			int[] inputSamples = null;
			synchronized (this) {
				s.read(inputSamples);
				if (inputSamples != null) {
					for (int sample : inputSamples) {
						fileBytes[0] = (byte) (sample >> 24);
						fileBytes[1] = (byte) (sample >> 16);
						fileBytes[3] = (byte) (sample >> 8);

						try {
							fileOutStream.write(fileBytes);
						} catch (IOException e) {
							System.out.println("Error writing track");
						}
					}
				}
			}
		}
	}

	public void record( String trackFile ) throws IOException {
		if (in != null) {
			try {
				OutputStream file = new FileOutputStream(trackFile);
				synchronized (this) {
					fileOutStream = new BufferedOutputStream(file);
				}

				Scanner scanner = new Scanner(System.in);
				scanner.next();
				scanner.close();
				synchronized (this) {
					fileOutStream.close();
					fileOutStream = null;
				}
			} catch (FileNotFoundException e) {
				System.out.println("Not found: " + trackFile);
				return;
			} catch (IOException e) {
				System.out.println("Error closing: " + trackFile);
			}

		}
		else {
			System.out.println( "No input stream for record" );
		}		
	}
	
	public AudioStream getInput() {
		return in;
	}
	
	public void setInput( AudioStream in ) {
		this.in = in;
		in.addSyncListener( this );
	}
	
	public AudioStream getOutput() {
		return out;
	}
	
	public void setOutput( AudioStream out ) {
		
	}

	public static void main( String args[] ) {
	}
}
