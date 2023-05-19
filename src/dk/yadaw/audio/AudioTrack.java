package dk.yadaw.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

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
	private int sampleRate;
	private DecimalFormat df;
	private int posd;
	private int[] tempOutSamples;
	private boolean fileReadCompleted;
	private int playSamplePos;

	/**
	 * Constructor
	 * 
	 * @param name     Name of track
	 * @param filePath Path to store track files
	 */
	public AudioTrack(int sampleRate, int sampleBufferSize ) {
		fileBytes = new byte[3];
		tempOutSamples = new int[8 * sampleBufferSize];
		this.sampleRate = sampleRate;
		df = new DecimalFormat("#.###");
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
	
	public void playbackStart( String trackFile ) throws IOException {
		playSamplePos = 0;
		if( out != null ) {
			try {
				System.out.println("Opening playback file: " + trackFile);
				InputStream file = new FileInputStream(trackFile);
				fileInStream = new BufferedInputStream(file);
				fileReadCompleted = false;
				handleOutputBufferTransfer( out );
			} catch (FileNotFoundException e) {
				System.out.println("Not found: " + trackFile);
				return;
			}
		} else {
			System.out.println("No output stream for playback");
		}
	}

	public AudioStream getInput() {
		return in;
	}

	public void setInput(AudioStream in) {
		this.in = in;
		in.addSyncListener(this);
	}

	public AudioStream getOutput() {

		return out;
	}

	public void setOutput(AudioStream out) {
		this.out = out;
		out.addSyncListener(this);
	}

	@Override
	public void audioSync(AudioStream s) {		
		if( in != null ) {
			handleInputBufferTransfer( s );
		}
		
		if( out != null ) {
			handleOutputBufferTransfer( s );
		}
	}
	
	private void handleOutputBufferTransfer( AudioStream s ) {
		if( fileInStream != null ) {
			if( fileReadCompleted ) {
				if( out.isEmpty() ) {
					System.out.println( "\nPlayback done");
					synchronized( this ) {
						notify();
					}
				}
			}
			else {
				while (!out.isFull() && !fileReadCompleted) {
					int n = 0;
					synchronized (fileInStream) {
						try {
							while (n < tempOutSamples.length) {
								int rdlen = fileInStream.read(fileBytes);
								if (rdlen == fileBytes.length) {
									tempOutSamples[n++] = ((fileBytes[0] & 0xff) << 24) | ((fileBytes[1] & 0xff) << 16)
											| ((fileBytes[0] & 0xff) << 8);
								} else {
									fileInStream.close();
									fileReadCompleted = true;
									break;
								}
							}

							if (n > 0) {
								int[] playBuffer = new int[n];
								System.arraycopy(tempOutSamples, 0, playBuffer, 0, n);
								out.write(playBuffer, playSamplePos);
								playSamplePos += n;
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if( in == null && ++posd >= 10 ) {
					float playTime = ( float )playSamplePos / ( float )sampleRate;
					System.out.print( "\r" + df.format(playTime) );
					posd = 0;
				}
			}
		}
	}
	
	private void handleInputBufferTransfer( AudioStream s ) {
		if ( fileOutStream != null) {
			AudioStreamBuffer abuf = s.read();
			if (abuf != null) {
				long spos = abuf.getSamplePos();
				int[] inputSamples = abuf.getBuffer();

				if (inputSamples != null) {
					if (++posd >= 10) {
						float timecode = (float) spos / (float) sampleRate;
						System.out.print("\r" + df.format(timecode));
						posd = 0;
					}

					synchronized (fileOutStream) {
						for (int n = 0; n < inputSamples.length; n++) {
							fileBytes[0] = (byte) (inputSamples[n] >> 24);
							fileBytes[1] = (byte) (inputSamples[n] >> 16);
							fileBytes[2] = (byte) (inputSamples[n] >> 8);
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
	}
	
	public static void main(String args[]) {
	}
}
