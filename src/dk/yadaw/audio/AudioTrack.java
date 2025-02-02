package dk.yadaw.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents and audio track having both an input stream for recording and
 * an output stream for playback.
 * 
 * @author tkn
 *
 */
public class AudioTrack implements SyncListener {
	private AudioStream in;
	private AudioStream out;
	private BufferedOutputStream fileOutStream;
	private BufferedInputStream fileInStream;
	private byte[] fileBytes;
	private boolean fileReadCompleted;
	private int readSamples;
	private boolean stopPlaybackWhenFileRead;

	/**
	 * Constructor
	 * 
	 * @param name     Name of track
	 * @param filePath Path to store track files
	 */
	public AudioTrack() {
		fileBytes = new byte[3];
	}

	public void recordStart(String trackFile) throws IOException {
		if (in != null) {
			try {
				System.out.println("Opening record file: " + trackFile);
				OutputStream file = new FileOutputStream(trackFile);
				fileOutStream = new BufferedOutputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println("Not found: " + trackFile);
				return;
			}
		} else {
			System.out.println("No input stream for record");
		}
	}

	public void recordStop() {
		synchronized (fileOutStream) {
			try {
				System.out.println("Closing track file");
				fileOutStream.close();
			} catch (IOException e) {
				System.out.println("Error closing track file");
			}
		}
	}
	
	public void playbackStart( String trackFile, boolean stopWhenFileRead ) throws IOException {
		if( out != null ) {
			try {
				System.out.println("Opening playback file: " + trackFile);
				InputStream file = new FileInputStream(trackFile);
				fileInStream = new BufferedInputStream(file);
				fileReadCompleted = false;
				readSamples = 0;
				stopPlaybackWhenFileRead = stopWhenFileRead;
				handleOutputBufferTransfer();
			} catch (FileNotFoundException e) {
				System.out.println("Not found: " + trackFile);
				return;
			}
		} else {
			System.out.println("No output stream for playback");
		}
	}
	
	public void playbackStop() {
		synchronized (fileInStream ) {
			fileReadCompleted = true;
			try {
				fileInStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public AudioStream getInput() {
		return in;
	}

	public void setInput(AudioStream in) {
		System.out.println( "AudioTrack set " + in + " as input ");
		this.in = in;
	}

	public AudioStream getOutput() {
		return out;
	}

	public void setOutput(AudioStream out) {
		System.out.println( "AudioTrack set " + out + " as output ");
		this.out = out;
	}

	@Override
	public void audioSync( long samplePos ) {
		if( in != null ) {
			handleInputBufferTransfer();
		}
		
		if( out != null ) {
			handleOutputBufferTransfer();
		}		
	}
	
	private void handleOutputBufferTransfer() {
		if( fileInStream != null ) {
			if( fileReadCompleted ) {
				if (stopPlaybackWhenFileRead) {
					if (out.isEmpty()) {
						System.out.println("\nPlayback done " + readSamples + " or " + readSamples / 48000 + "s");
						fileInStream = null;
						synchronized (this) {
							notify();
						}
					}
				}
				else {
					while( !out.isFull()  ) {
						out.write( 0 );
					}
				}
			}
			else {
				synchronized (fileInStream) {
					while (!out.isFull() && !fileReadCompleted) {
						try {
							int rdlen = fileInStream.read(fileBytes);
							if (rdlen == fileBytes.length) {
								int sample = ((fileBytes[0] & 0xff) << 24) | ((fileBytes[1] & 0xff) << 16)
										| ((fileBytes[0] & 0xff) << 8);
								out.write( sample );
								readSamples++;
							} 
							else {
								fileInStream.close();
								fileReadCompleted = true;
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void handleInputBufferTransfer() {
		if ( fileOutStream != null) {
			synchronized (fileOutStream) {
				while( in.available() > 0 ) {
					int sample = in.read();
					fileBytes[0] = (byte) (sample >> 24);
					fileBytes[1] = (byte) (sample >> 16);
					fileBytes[2] = (byte) (sample >> 8);
					try {
						fileOutStream.write(fileBytes);
					} catch (IOException e) {
						System.out.println("Error writing track");
					}
				}
			}
		}
	}
	
	public static void main(String args[]) {
	}
}
