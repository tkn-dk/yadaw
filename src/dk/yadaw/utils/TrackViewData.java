package dk.yadaw.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import dk.yadaw.audio.AudioStream;

public class TrackViewData {
	private InputStream file;
	private BufferedInputStream inStream;
	private ArrayList<Integer> peaks;
	private long samplePosStart;
	private long samplePosEnd;
	private int samplesPerPeak;
	private AudioStream recordStream;
	private int recPmax;
	private int recPmin;
	private int peakSample;

	public TrackViewData() {
		peaks = new ArrayList<Integer>();
	}

	public void setPeakWindow(long samplePosStart, long samplePosEnd, int samplesPerPeak) {
		this.samplePosStart = samplePosStart;
		this.samplePosEnd = samplePosEnd;
		this.samplesPerPeak = samplesPerPeak;
	}

	public boolean loadTrackFile(String trackFile) {
		try {
			file = new FileInputStream(trackFile + ".raw");
			inStream = new BufferedInputStream(file);
			loadPeakArray();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("TrackViewData " + trackFile + " not found");
		}
		return false;
	}

	public long getNofSamples(String trackFile) {
		File f = new File(trackFile + ".raw");
		if (f.exists()) {
			return f.length() / 3;
		}
		System.out.println("TrackViewData " + trackFile + " not found.");
		return 0;
	}

	public int[] getPeakArray() {
		return peaks.stream().mapToInt(Integer::intValue).toArray();
	}

	public void setRecordStream(AudioStream stream) {
		recordStream = stream;
	}

	public void recordAudioSync(long samplePos) {
		if ( recordStream != null && samplesPerPeak > 0) {
			while (!recordStream.isEmpty()) {
				int s = recordStream.read();
				if (s > recPmax) {
					recPmax = s;
				} else if (s < recPmin) {
					recPmin = s;
				}

				if (++peakSample >= samplesPerPeak) {
					System.out.print("!");
					peakSample = 0;
					peaks.add(Math.max(recPmax, Math.abs(recPmin)));
					recPmax = 0;
					recPmin = 0;
				}
			}
		}
	}

	private void loadPeakArray() {
		int sampleCount = (int) (samplePosEnd - samplePosStart);
		int count = sampleCount / samplesPerPeak;

		peaks.clear();
		byte[] fileBytes = new byte[3];
		try {
			inStream.skip(samplePosStart * 3);
			for (int n = 0; n < count; n++) {
				int pmax = 0;
				int pmin = 0;
				for (int p = 0; p < samplesPerPeak; p++) {
					int rdlen = inStream.read(fileBytes);
					if (rdlen == fileBytes.length) {
						int sample = ((fileBytes[0] & 0xff) << 24) | ((fileBytes[1] & 0xff) << 16)
								| ((fileBytes[0] & 0xff) << 8);
						if (sample > pmax) {
							pmax = sample;
						} else if (sample < pmin) {
							pmin = sample;
						}
					} else {
						break;
					}
				}
				peaks.add(Math.max(pmax, Math.abs(pmin)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
