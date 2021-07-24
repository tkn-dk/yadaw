package dk.yadaw.main;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
		
		JMenuBar menuBar = new JMenuBar();
		
		// File menu
		JMenu fileMenu = new JMenu( "File" );
		JMenuItem fileSave = new JMenuItem( "Save..." );
		JMenuItem fileLoad = new JMenuItem( "Load..." );
		JMenuItem fileExit = new JMenuItem( "Exit" );
		fileExit.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(NORM_PRIORITY);	
			}
		});
		fileMenu.add( fileSave );
		fileMenu.add( fileLoad );
		fileMenu.add( fileExit );
		menuBar.add( fileMenu );
		
		// Audio menu
		JMenu audioMenu = new JMenu( "Audio" );
		JMenuItem audioSelectInterface = new JMenu( "Open Interface");
		JMenuItem audioAddTrack = new JMenu( "Add track" );
		audioMenu.add( audioSelectInterface );
		audioMenu.add( audioAddTrack );
		menuBar.add( audioMenu );
		
		mainFrame.setJMenuBar(menuBar);
	}
	
	@Override
	public void run() {
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
