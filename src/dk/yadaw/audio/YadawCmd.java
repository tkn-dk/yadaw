package dk.yadaw.audio;

import java.util.Collection;

public class YadawCmd {
	private Asio asio; 
	
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
		AudioTrack track = new AudioTrack();
		AudioStream rstream = new AudioStream();
		asio.setInput( channel, rstream);
		track.setInput(rstream);
	}
	
	public void play( String fileName, int channel ) {
		
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
}
