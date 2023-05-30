package dk.yadaw.main;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.Mixer;
import dk.yadaw.audio.MixerChannel;
import dk.yadaw.datamodel.DataEvent;
import dk.yadaw.datamodel.DataItemID;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.DataModelUpdateListenerIf;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.widgets.TrackPanel;

/**
 * Controller for Yadaw.
 * @author tkn
 *
 */
public class YadawController implements DataModelUpdateListenerIf {
	YadawDataModel yaModel;
	YadawFrame yaFrame;
	Asio asio;
	Collection<TrackController> trackControllers;
	
	public YadawController() {
		yaModel = DataModelInstance.getModelInstance();
		yaModel.addUpdateListener( DataItemID.YADAW_ALL, this );
		asio = yaModel.getAsio();
		trackControllers = new ArrayList<TrackController>();
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				yaFrame = new YadawFrame();
				yaFrame.setVisible(true);
			}
			
		});
	}
	
	private void setupMixer() {
		Mixer mixer = new Mixer( asio.getBufferSize() );
		mixer.setMaster( new AudioStream(), new AudioStream());
		if( asio.connectInput(0, mixer.getMasterLeft()) ) {
			System.out.println( "Left connect" );
		}
		else {
			return;
		}
		
		if( asio.connectInput(0, mixer.getMasterRight()) ) {
			System.out.println( "Right connect");
		}
		else {
			return;
		}
		
		yaModel.setMixer(mixer);
		
		for( int ch = 0; ch < asio.getNofInputs(); ch++ ) {
			String label = "IN" + (ch+1);
			MixerChannel mxc = new MixerChannel( label, yaModel.getNumSends());
			mxc.setIn( new AudioStream() );
			mixer.addChannel( mxc );
			TrackPanel tp = yaFrame.addPanel(label);
			trackControllers.add( new TrackController( mxc, tp ));
			
			mxc.setIn( new AudioStream() );
			if( asio.connectOutput( ch, mxc.getIn() )) {
				System.out.println( "Mixer channel " + mxc.getLabel() + " connected to ASIO output stream (analog in)");
			}
			else {
				return;
			}
		}
		
		asio.start();
	}

	@Override
	public void dataItemUpdated( DataEvent dEvent ) {
		switch( dEvent.getID() ) {
		case YADAW_ASIO_DRIVER_NAME:
			System.out.println( "Opening: " + yaModel.getAsioDriverName() );
			try {
				asio.openDriver( yaModel.getAsioDriverName() );
				setupMixer();				
			} catch (AsioException e) {
				e.printStackTrace();
			}
			break;
			
		case YADAW_ASIO:
			System.out.println( "Datamodel asio update");
			asio = yaModel.getAsio();
			break;
			
		default:
			break;
		}

	}
	
	
	

}
