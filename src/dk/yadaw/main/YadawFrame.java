package dk.yadaw.main;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import dk.yadaw.widgets.SelectAsioDlg;
import dk.yadaw.widgets.TrackPanel;
import dk.yadaw.widgets.ViewAudioParmsDlg;

public class YadawFrame extends JFrame {
	
	public YadawFrame() {
		super( "Yadaw" );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setExtendedState( JFrame.MAXIMIZED_BOTH );
		setLayout( new GridLayout( 0, 1 ) );

		TrackPanel[] trackPanels = new TrackPanel[8];
		for( int n = 0; n < trackPanels.length; n++ ) {
			trackPanels[n] = new TrackPanel( n + 1);
			add(trackPanels[n]);	
		}
		
		pack();
		setVisible(true);
		
		JMenuBar menuBar = new JMenuBar();
		
		// File menu
		JMenu fileMenu = new JMenu( "File" );
		JMenuItem fileSave = new JMenuItem( "Save..." );
		JMenuItem fileLoad = new JMenuItem( "Load..." );
		JMenuItem fileExit = new JMenuItem( "Exit" );
		fileExit.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit( 0 );
			}
		});
		fileMenu.add( fileSave );
		fileMenu.add( fileLoad );
		fileMenu.add( fileExit );
		menuBar.add( fileMenu );
		
		// Audio menu
		JFrame listener = this;
		JMenu audioMenu = new JMenu( "Audio" );
		JMenuItem audioSelectInterface = new JMenuItem( "Open Interface");
		audioSelectInterface.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println( "Open asio dialog");
				SelectAsioDlg dlg = new SelectAsioDlg( listener );
			}
		});
		
		JMenuItem audioSettings = new JMenuItem( "Audio Settings");
		audioSettings.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println( "Audio settings action " );
				ViewAudioParmsDlg dlg = new ViewAudioParmsDlg( listener );
			}
		});
		
		JMenuItem audioAddTrack = new JMenuItem( "Add track" );
		audioMenu.add( audioSelectInterface );
		audioMenu.add( audioSettings );
		audioMenu.add( audioAddTrack );
		menuBar.add( audioMenu );
		
		setJMenuBar(menuBar);
		
		addComponentListener( new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				for( int n = 0; n < trackPanels.length; n++ ) {
					trackPanels[n].resizeTrackView();
				}
				super.componentResized(e);
			}
			
		});
	}

}
