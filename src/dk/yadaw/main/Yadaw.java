package dk.yadaw.main;



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
		JMenu fileMenu = new JMenu( "File" );
		JMenuItem fileSave = new JMenuItem( "Save..." );
		JMenuItem fileLoad = new JMenuItem( "Load..." );
		JMenuItem fileExit = new JMenuItem( "Exit" );
		fileMenu.add( fileSave );
		fileMenu.add( fileLoad );
		fileMenu.add( fileExit );
		menuBar.add( fileMenu );
		
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
