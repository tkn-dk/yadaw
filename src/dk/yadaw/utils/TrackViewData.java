package dk.yadaw.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TrackViewData {
	private InputStream file;
	private BufferedInputStream inStream;
	private String trackFile;
	
	public TrackViewData( String trackFile ) {
		this.trackFile = trackFile + ".raw";
		try {
			file = new FileInputStream( this.trackFile );
			inStream = new BufferedInputStream( file );
		} catch (FileNotFoundException e) {
			System.out.println( "TrackViewData " + trackFile + " not found");
		}
	}

	public long getNofSamples() {
		File f = new File( trackFile );
		if( f.exists() ) {
			return f.length() / 3;
		}
		return 0;
	}
	
	public int[] getPeakArray( long fstart, long fend, int samplesPerPeak ) {
		int sampleCount = ( int )(fend - fstart);
		int count = sampleCount / samplesPerPeak;
		int[] peakArray = new int[count];
		byte[] fileBytes = new byte[3];
		
		try {
			inStream.skip( fstart * 3 );
			for( int n = 0; n < count; n++ ) {
				int pmax = 0;
				int pmin = 0;
				for( int p = 0; p < samplesPerPeak; p++ ) {
					int rdlen = inStream.read( fileBytes );
					if (rdlen == fileBytes.length) {
						int sample = ((fileBytes[0] & 0xff) << 24) | ((fileBytes[1] & 0xff) << 16)
								| ((fileBytes[0] & 0xff) << 8);
						if( sample > pmax ) {
							pmax = sample;
						} else if( sample < pmin ) {
							pmin = sample;
						}
					} else {
						break;
					}
				}
				peakArray[n] = Math.max( Math.abs(pmax), Math.abs(pmin));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peakArray;
	}
}
