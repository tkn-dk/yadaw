package dk.yadaw.audio;

public class AsioOutChannel implements SoundOutStream {
	String sampleFileName;
	int sampleRate;
	long samplePos;
	int[] buffer;
	int in;
	int out;
	
	public AsioOutChannel( int sampleRate ) {
		this.sampleRate = sampleRate;
		buffer = new int[sampleRate];
		in = 0;
		out = 0;
	}
	
	public void setRawSampleFileName( String fileName ) {
		sampleFileName = fileName;
	}

	public int write(int[] samples, int nofSamples) {
		int n;
		for( n = 0; n < nofSamples; n++ ) {
			if( in == out ) {
				break;
			}
			buffer[in++] = samples[n];
			if( in == sampleRate ) {
				in = 0;
			}
		}
		return n;
	}


}
