package dk.yadaw.main;

import java.awt.Color;
import java.util.Collection;
import javax.swing.SwingUtilities;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AsioException;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.Mixer;
import dk.yadaw.audio.MixerChannel;
import dk.yadaw.audio.SyncListener;
import dk.yadaw.datamodel.DataEvent;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.DataModelUpdateListenerIf;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.widgets.TrackPanel;

/**
 * Controller for Yadaw.
 * 
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
	private int nextTrackNumber;

	public YadawController() {
		yaModel = DataModelInstance.getModelInstance();
		yaModel.addUpdateListener(YadawDataModel.DataItemID.YADAW_ALL, this);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				yaFrame = new YadawFrame();
			}

		});

		soundThread = new Thread() {
			@Override
			public void run() {
				if (!asio.start()) {
					System.out.println("Asio start error ");
					return;
				}
			}
		};
	}

	private void connectMixer() {

		if (asio.connectInput(0, mixer.getMasterLeft())) {
			System.out.println("Left connect");
		}

		if (asio.connectInput(1, mixer.getMasterRight())) {
			System.out.println("Right connect");
		} else {
			return;
		}

		setPlaybackAndRecording();
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
				connectMixer();				
			} catch (AsioException e) {
				e.printStackTrace();
			}
			break;
			
		case YADAW_ASIO:
			System.out.println( "Datamodel asio update");
			asio = yaModel.getAsio();
			mixer = new Mixer( asio.getOutputLock() );
			mixer.setMaster( new AudioStream( "master L"), new AudioStream( "master R"));
			yaModel.setMixer(mixer);
			trackControllers = yaModel.getTrackControllers();
			break;
			
		case YADAW_TRACKCONTROLLER_ADD:
			System.out.println( "DataModel trackController added");
			break;
			
		case YADAW_UI_OPERATION:
			handleUIOperation( ( YadawDataModel.UIOperation )dEvent.getDataItem() );
			break;
			
		default:
			break;
		}

	}

	private void handleUIOperation(YadawDataModel.UIOperation op) {
		switch (op) {
		case UI_ADD_TRACK:
			addTrack();
			break;

		default:
			break;

		}
	}

	private void addTrack() {
		yaFrame.newConsolidatedPanel();
		String label = "TRACK" + nextTrackNumber++;
		MixerChannel mxc = new MixerChannel(label, 0, yaModel.getNumSends());
		mixer.addChannel(mxc);
		TrackPanel tp = new TrackPanel(label, new Color(128, 128, 128), 1, yaModel.getMixerFont());
		yaModel.addTrackController(new TrackController(yaFrame, label, mxc, tp));
		yaFrame.addConsolidatedPanel(tp);
		yaFrame.commitConsolidatedPanels();
	}

	private void setPlaybackAndRecording() {
		int ch = 0;
		for (TrackController tc : trackControllers) {
			tc.setPlaybackOrRecord(asio);
			if (tc.isRecording()) {
				System.out.println("Record on " + tc.getMixerChannel().getLabel());
				MixerChannel mxc = tc.getMixerChannel();
				if (asio.connectOutput(ch, mxc.getIn())) {
					System.out.println(
							"Mixer channel " + mxc.getLabel() + " connected to ASIO output stream (analog in)");
				} else {
					System.out.println("ASIO output connect error");
				}
			} else {
				System.out.println("Playback on " + tc.getMixerChannel().getLabel());
			}

		}
	}

	@Override
	public void audioSync(long samplePos) {
		for (TrackController tc : trackControllers) {
			tc.audioSync(samplePos);
		}
		mixer.audioSync(samplePos);
	}

}
