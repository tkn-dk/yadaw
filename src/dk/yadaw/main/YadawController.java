package dk.yadaw.main;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.Mixer;
import dk.yadaw.audio.MixerChannel;
import dk.yadaw.audio.SyncListener;
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
public class YadawController implements DataModelUpdateListenerIf, SyncListener {
	private YadawDataModel yaModel;
	private YadawFrame yaFrame;
	private Thread soundThread;
	private Asio asio;
	private Mixer mixer;
	Collection<TrackController> trackControllers;
	
	public YadawController() {
		yaModel = DataModelInstance.getModelInstance();
		yaModel.addUpdateListener( DataItemID.YADAW_ALL, this );
		asio = yaModel.getAsio();
		trackControllers = yaModel.getTrackControllers();
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				yaFrame = new YadawFrame();
			}
			
		});
		
		soundThread = new Thread() {
			@Override
			public void run() {
				if( !asio.start() ) {
					System.out.println( "Asio start error ");
					return;
				}				
			}			
		};
	}
	
	private void setupMixer() {
		mixer = new Mixer( asio.getOutputLock() );
		yaModel.setMixer(mixer);
		mixer.setMaster( new AudioStream( "master L"), new AudioStream( "master R"));
		
		if( asio.connectInput(0, mixer.getMasterLeft()) ) {
			System.out.println( "Left connect" );
		}
		else {
			return;
		}
		
		if( asio.connectInput(1, mixer.getMasterRight()) ) {
			System.out.println( "Right connect");
		}
		else {
			return;
		}
		
		yaFrame.newConsolidatedPanel();
		
		Color[] trackColors = new Color[] { Color.RED, Color.GREEN, Color.ORANGE, Color.YELLOW };
		for( int ch = 0; ch < 4; ch++ ) {
			String label = "IN" + (ch+1);
			MixerChannel mxc = new MixerChannel( label, ch, yaModel.getNumSends());
			mixer.addChannel( mxc );
			TrackPanel tp =  new TrackPanel( label, trackColors[ch], ch + 1, yaModel.getMixerFont());
			trackControllers.add( new TrackController( yaFrame, label, mxc, tp ));
			yaFrame.addConsolidatedPanel(tp);
		}
		
		yaFrame.commitConsolidatedPanels();
		setPlaybackAndRecording();
		
		/*AudioTrack track = new AudioTrack();
		AudioStream playStream = new AudioStream();
		asio.connectInput(0, playStream);
		track.setOutput(playStream);
		try {
			track.playbackStart( "IN1.raw");
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		asio.setSyncListener(this);
		
		soundThread.start();
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
	
	
	private void setPlaybackAndRecording() {
		int ch = 0;
		for( TrackController tc : trackControllers ) {
			tc.setPlaybackOrRecord( asio );
			if( tc.isRecording() ) {
				System.out.println( "Record on " + tc.getMixerChannel().getLabel() );
				MixerChannel mxc = tc.getMixerChannel();
				if( asio.connectOutput( ch, mxc.getIn() )) {
					System.out.println( "Mixer channel " + mxc.getLabel() + " connected to ASIO output stream (analog in)");
				}
				else {
					System.out.println( "ASIO output connect error");
				}			
			}
			else {
				System.out.println( "Playback on " + tc.getMixerChannel().getLabel() );
			}

		}
	}

	@Override
	public void audioSync(long samplePos) {
		for( TrackController tc : trackControllers ) {
			tc.audioSync(samplePos);
		}
		mixer.audioSync(samplePos);
	}
	
}
