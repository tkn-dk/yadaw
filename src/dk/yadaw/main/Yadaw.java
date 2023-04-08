package dk.yadaw.main;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.DataModelUpdateListenerIf;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.datamodel.YadawDataModel.DataID;
import dk.yadaw.widgets.SelectAsioDlg;
import dk.yadaw.widgets.VUMeter;
import dk.yadaw.widgets.ViewAudioParmsDlg;

public class Yadaw extends Thread implements DataModelUpdateListenerIf {
	VUMeter vu;
	JFrame mainFrame;
	YadawDataModel model;
	Asio asio;
	
	public void initApp() {
		model = DataModelInstance.getModelInstance();
		model.addUpdateListener( DataID.YADAW_ALL, this );
		asio = model.getAsio();
		createGui();
	}
	
	private void createGui() {
		mainFrame = new JFrame( "Yadaw" );
		mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		mainFrame.setExtendedState( JFrame.MAXIMIZED_BOTH );
		
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
		JMenuItem audioSelectInterface = new JMenuItem( "Open Interface");
		audioSelectInterface.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println( "Open asio dialog");
				SelectAsioDlg dlg = new SelectAsioDlg( mainFrame );
			}
		});
		
		JMenuItem audioSettings = new JMenuItem( "Audio Settings");
		audioSettings.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println( "Audio settings action " );
				ViewAudioParmsDlg dlg = new ViewAudioParmsDlg( mainFrame );
			}
		});
		
		JMenuItem audioAddTrack = new JMenuItem( "Add track" );
		audioMenu.add( audioSelectInterface );
		audioMenu.add( audioSettings );
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
				yoda.initApp();
				yoda.start();
			}
		});
	}

	@Override
	public void dataItemUpdated(Object itemID, Object itemData) {
		DataID dataID = ( DataID)itemID;
		
		switch( dataID ) {
		case YADAW_ASIO_DRIVER_NAME:
			System.out.println( "Opening: " + ( String )itemData );
			try {
				asio.openDriver( model.getAsioDriverName() );
			} catch (AsioException e) {
				e.printStackTrace();
			}
			break;
			
		default:
			break;
		}
	}

}
