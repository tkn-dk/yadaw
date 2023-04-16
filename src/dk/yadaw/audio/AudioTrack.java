package dk.yadaw.audio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * Represents and audio track having both producer and consumer capabilities.
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

	/**
	 * Constructor
	 * 
	 * @param name     Name of track
	 * @param filePath Path to store track files
	 */
	public AudioTrack(int sampleRate) {
		fileBytes = new byte[3];
		this.sampleRate = sampleRate;
		df = new DecimalFormat("#.###");
	}

	@Override
	public void audioSync(AudioStream s) {
		if (s == in && fileOutStream != null) {
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

	public void recordStart(String trackFile) throws IOException {
		if (in != null) {
			try {
				System.out.println("Opening file: " + trackFile);
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
	}

	public static void main(String args[]) {
	}
}
