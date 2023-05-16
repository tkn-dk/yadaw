package dk.yadaw.main;

import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.DataModelUpdateListenerIf;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.datamodel.YadawDataModel.DataID;

/**
 * Controller for Yadaw.
 * @author tkn
 *
 */
public class YadawController implements DataModelUpdateListenerIf {
	YadawDataModel yaModel;
	YadawFrame yaFrame;
	Asio asio;
	
	public YadawController() {
		yaModel = DataModelInstance.getModelInstance();
		yaModel.addUpdateListener(YadawDataModel.DataID.YADAW_ALL, this );
		asio = yaModel.getAsio();
		
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				yaFrame = new YadawFrame();
				yaFrame.setVisible(true);
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
				asio.openDriver( yaModel.getAsioDriverName() );
			} catch (AsioException e) {
				e.printStackTrace();
			}
			break;
			
		default:
			break;
		}

	}
	
	
	

}
