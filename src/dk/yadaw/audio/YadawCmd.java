package dk.yadaw.audio;

import java.io.IOException;
import java.util.Collection;

public class YadawCmd implements SyncListener {
	private Asio asio; 
	private Thread asioThread;
	private AudioStream rstream;
	private AudioStream pLeftStream;
	private AudioStream pRightStream;
	private AudioStream pTrackStream;
	private AudioTrack track;	

	private int[] delayBuffer;
	int drp;
	int dwp;
	
	public YadawCmd() {
		asio = new Asio();
	}

	public Asio getAsio() {
		return asio;
	}
	
	public boolean openDriver( String driverName ) {
		
		try {
			asio.openDriver(driverName);
		} catch (AsioException e) {
			System.out.println( e );
			return false;
		}
		
		return true;
	}
	
	public void record( String fileName, int channel ) {
		track = new AudioTrack();
		rstream = new AudioStream();
		asio.connectOutput( channel, rstream);
		track.setInput(rstream);
		
		try {
			track.recordStart(fileName);
		} catch (IOException e1) {
			System.out.println( "Could not write trackfile: " + fileName );
		}

		startSound();
		System.out.println( "Press enter to stop recording");
		try {
			System.in.read();
		} catch (IOException e) {
			System.out.println( "  system in error" );
		}
		System.out.println( "Stopping");
		asio.stop();
		track.recordStop();
		
		try {
			asioThread.interrupt();
			asioThread.join();
			System.out.println( "Asio thread joined");
		} catch (InterruptedException e) {
			System.out.println( "ASIO thread join interrupted" );
		}		
	}
	
	public void play( String fileName, int channel ) {
		track = new AudioTrack();
		pLeftStream = new AudioStream();
		pRightStream = new AudioStream();
		pTrackStream = new AudioStream();
		
		asio.connectInput(0, pLeftStream);
		asio.connectInput(1, pRightStream);
		track.setOutput( pTrackStream );
		
		try {
			track.playbackStart(fileName, true);
		} catch (IOException e) {
			System.out.println( "Could not open trackfile: " + fileName );
		}
		
		delayBuffer = new int[24000];
		dwp = 256;
		drp = 0;
		transferStreams();
		
		startSound();
		try {
			synchronized( track ) {
				track.wait();
			}
		} catch( InterruptedException e ) {
			System.out.println( "Playback stopped");
		}
		
		asio.stop();		
	}
	
	public void startSound() {
		asio.setSyncListener(this);
		asioThread = new Thread() {

			@Override
			public void run() {
				if( !asio.start() ) {
					System.out.println( "Asio start error ");
					return;
				}
			}
			
		};
		asioThread.start();		
	}
	
	public static void main(String[] args) {
		System.out.println( "Yadaw Commandline DAW");
		
		if( args.length == 4 ) {
			
			String asioDriverName = args[0];
			String recPlay = args[1];
			int ch = Integer.parseInt(args[2]);
			String fileName = args[3];
			
			System.out.println(
					"Using " + asioDriverName + " to " + recPlay + " channel: " + ch + " to file: " + fileName);

			YadawCmd ycmd = new YadawCmd();
			if (ycmd.openDriver(asioDriverName)) {
				switch (recPlay.toLowerCase()) {
				case "record":
					ycmd.record(fileName, ch);
					System.out.println( "YadawCmd record return");
					break;

				case "play":
					ycmd.play(fileName, ch);
					break;

				default:
					System.out.println("Unknown command: " + recPlay);
				}
			}
			else {
				System.out.println( "Could not open driver: " + asioDriverName );
				System.out.println( "Available drivers: ");
				Collection<String> availableDrivers = ycmd.getAsio().getDrivers();
				for( String d : availableDrivers ) {
					System.out.println( "  " + d );
				}
			}
		}
		else {
			System.out.println( "  usage: YadawCmd <\"asio driver name\"> <record | play> <channel> <file>" );
		}
	}

	private void transferStreams() {
		while( pTrackStream.available() > 0 ) {
			int inSample = pTrackStream.read();
			delayBuffer[dwp] = inSample;
			dwp++;
			if( dwp == delayBuffer.length )
				dwp = 0;
			int dSample = delayBuffer[drp++];
			if( drp == delayBuffer.length )
				drp = 0;
			
			pLeftStream.write( inSample );
			pRightStream.write( inSample );
		}
	}
	
	@Override
	public void audioSync(long samplePos) {
		if( pTrackStream != null ) {
			transferStreams();
			pTrackStream.sync(samplePos);
			pLeftStream.sync(samplePos);
			pRightStream.sync(samplePos);
			track.audioSync(samplePos);		
		}
		else if( rstream != null ) {
			rstream.sync(samplePos);
			track.audioSync(samplePos);
		}
	}
}
