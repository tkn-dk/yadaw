package dk.yadaw.main;



import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.DataModelUpdateListenerIf;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.datamodel.YadawDataModel.DataID;

public class Yadaw extends Thread implements DataModelUpdateListenerIf {
	YadawFrame mainFrame;
	YadawDataModel model;
	Asio asio;
	
	public void initApp() {
		model = DataModelInstance.getModelInstance();
		model.addUpdateListener( DataID.YADAW_ALL, this );
		asio = model.getAsio();
		mainFrame = new YadawFrame();
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
