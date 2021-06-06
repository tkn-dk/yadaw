package dk.yadaw.recorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

public class TrackRecorder {
	
	private TargetDataLine lineIn;
	private int inputChannel;
	private int bufferSize;
	private String identifier;
	AudioFormat audioFormat;
	
	public TrackRecorder( AudioFormat aFormat, String identifier, int inputChannel, int bufferSize ) {
		this.inputChannel = inputChannel - 1;
		this.identifier = identifier;
		this.lineIn = altFindRecordLine();
		this.audioFormat = aFormat;
		this.bufferSize = bufferSize;
	}
	
	public TargetDataLine getLine() {
		return lineIn;
	}
	
	public boolean startRecord() {
		if( !lineIn.isOpen() ) {
			try {
				lineIn.open( audioFormat, bufferSize );
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		System.out.println( "Start!" );
		lineIn.start();		
		int bsize = 2 * 3 * 48000;
		byte buffer[] = new byte[bsize];
		int readBytes = lineIn.read(buffer, 0, bsize );
		lineIn.stop();
		System.out.println( "Stop! Read " + readBytes + " from line " );
		for( int n= 0; n < 50; n++ ) {
			int left = ( buffer[6*n] << 16 ) + ( buffer[6*n+1] << 8 ) + ( buffer[6*n+2] );
			int right = ( buffer[6*n+3] << 16 ) + ( buffer[6*n+4] << 8 ) + ( buffer[6*n+5] );
			System.out.println( "Left: " + String.format( "0x%04x", left) + "    Right: " + String.format( "0x%04x", right ) );
		}
		return true;
	}
	
	private TargetDataLine altFindRecordLine() {
		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat ); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println( "No line woth that format.");
	    }
		else {
			System.out.println( "Found line: " + info.toString() );
			try {
			    line = (TargetDataLine) AudioSystem.getLine(info);
			    AudioFormat fmt = line.getFormat();
			    System.out.println( "Format is: " + fmt.toString() );
			} catch (LineUnavailableException ex) {
				ex.printStackTrace();
			}
		}
		return line;
	}
	
	private TargetDataLine findRecordLine( ) {
		System.out.println( "findAndOpenLine" );
		Mixer.Info mixersInfo[] = AudioSystem.getMixerInfo();

		for( Mixer.Info mInfo : mixersInfo ) {
			String infoString = mInfo.toString();
			System.out.print( "  Mixer: " + infoString );
			
			Mixer mix = AudioSystem.getMixer(mInfo);
			Line.Info[] sourceInfo = mix.getSourceLineInfo();
			Line.Info[] targetInfo = mix.getTargetLineInfo();
			
			System.out.println( "  has " + sourceInfo.length + " source lines and " + targetInfo.length + " target lines." );
			if( targetInfo.length > 0 ) {
				Line.Info inst = targetInfo[inputChannel]; 
				if( inst instanceof DataLine.Info ) {
					DataLine.Info dlInfo = ( DataLine.Info )targetInfo[inputChannel];
					AudioFormat formats[] = dlInfo.getFormats();
					for( AudioFormat frm : formats ) {
						System.out.println( "    Format: " + frm.toString() );
					}
				}
				
				try {
					Line lineInstance;
					lineInstance = mix.getLine( targetInfo[inputChannel] );
					if( lineInstance instanceof TargetDataLine ) {
						System.out.println( "   IS target line" );
						return ( TargetDataLine )lineInstance;
					}
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void main( String args[] ) {
		AudioFormat af = new AudioFormat( 48000, 24, 2, false, true );
		TrackRecorder trc = new TrackRecorder( af, "Focusrite", 1, 256 ); 
//		trc.startRecord();
	}

}
