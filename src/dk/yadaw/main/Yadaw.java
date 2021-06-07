package dk.yadaw.main;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dk.yadaw.widgets.VUMeter;

public class Yadaw extends Thread {
	VUMeter vu;
	JFrame mainFrame;
	
	public void createGui() {
		mainFrame = new JFrame( "Yadaw" );
		mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		mainFrame.setSize(300, 300);
		
		vu = new VUMeter( false );
		mainFrame.add( vu );
		mainFrame.setVisible(true);
	}
	
	@Override
	public void run() {
		test();
	}
	
	private void test() {
		int vals[] = {0, 1, 2, 4, 11, 2, 3, 2, 3, 10, 2, 3, 2, 1, 9, 2, 1, 0, 1, 9 };
		int i = 0;
		for( int n = 0; n < 100; n++ ) {
			vu.setVal ( vals[i++] );
			if( i == vals.length ) {
				i = 0;
			}
			try {
				Thread.sleep( 10 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		vu.setVal( 0 );
		mainFrame.
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	public static void main( String args[] ) {
		Yadaw yoda = new Yadaw();
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				yoda.createGui();
				yoda.start();
			}
		});
	}

}
