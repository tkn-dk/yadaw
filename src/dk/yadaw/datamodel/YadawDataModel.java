package dk.yadaw.datamodel;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.Mixer;
import dk.yadaw.main.TrackController;
import dk.yadaw.widgets.InputLabel;

/**
 * Holds all data model objects.
 * @author tkn
 *
 */
public class YadawDataModel {
	public enum UIOperation { 
		UI_ADD_TRACK,
		UI_START,
		UI_STOP,
		UI_OPEN_ASIO 
	};
	
	public enum DataItemID {
		YADAW_ALL,
		YADAW_ASIO_DRIVER_NAME,
		YADAW_MIXER,
		YADAW_ASIO,
		YADAW_LABELFONT,
		YADAW_TRACKCONTROLLER_ADD,
		YADAW_UI_OPERATION
	};
	
	private final int numSends = 2;
	private Asio asio;
	private String asioDriverName;
	private Collection<DataListenerItem> dataListeners;
	private Mixer mixer;
	private Font mixerFont;
	private Thread dataUpdateThread;
	private LinkedList<DataEvent>eventQueue;
	private Collection<TrackController> trackControllers;

		
	/**
	 * Constructs data model object.
	 */
	public YadawDataModel() {
		mixerFont = new Font( "Ariel", Font.BOLD, 10 );
		
		eventQueue = new LinkedList<DataEvent>();
		dataUpdateThread = new Thread() {

			@Override
			public void run() {
				trackControllers = new ArrayList<TrackController>();
				boolean active = true;
				setAsio( new Asio());
				synchronized( this ) {
					while (active) {
						while (eventQueue.isEmpty()) {
							try {
								System.out.println( "DataModel update thread wait" );
								wait();
								System.out.println( "DataModel update thread notified" );
							} catch (InterruptedException e) {
								active = false;
								break;
							}
						}
						
						if( active && dataListeners != null ) {
							DataEvent dataEvent = eventQueue.poll();
							for( DataListenerItem dl : dataListeners ) {
								System.out.println( "Listener id: " + dl.getID().toString() + ". Event id: " + dataEvent.getID() );
								if( dl.getID() == DataItemID.YADAW_ALL || dataEvent.getID() == dl.getID() ) {
									System.out.println( "Sending event from data model thread, id: " + dataEvent.getID().toString());
									dl.getListener().dataItemUpdated(dataEvent);
								}
							}
						}
						else {
							System.out.println( "No update listeners - I guess.");
						}
						
					}
				}
			}			
		};
		dataUpdateThread.start();
	}

	/**
	 * Get a reference to Asio object.
	 * @return	Asio object.
	 */
	public Asio getAsio() {
		return asio;
	}
	
	public void setAsio( Asio asio ) {
		this.asio = asio;
		queueDataEvent( new DataEvent( YadawDataModel.DataItemID.YADAW_ASIO, this.asio ));
	}
	
	/**
	 * Sets the name of the used asio driver in the model.
	 * @param asioDriverName Asio driver name.
	 */
	public void setAsioDriverName( String asioDriverName ) {
		this.asioDriverName = asioDriverName;
		queueDataEvent(new DataEvent( DataItemID.YADAW_ASIO_DRIVER_NAME, this.asioDriverName ));
	}
	
	/**
	 * Returns the name of the used asio driver.
	 * @return Asio driver name.
	 */
	public String getAsioDriverName() {
		return asioDriverName;
	}
	
	/**
	 * Set system mixer
	 * @param mixer
	 */
	public void setMixer( Mixer mixer ) {
		this.mixer = mixer;
		queueDataEvent( new DataEvent( DataItemID.YADAW_MIXER, this.mixer ));
	}
	
	/**
	 * Return number of sends set in the system
	 * @return
	 */
	public int getNumSends() {
		return numSends;
	}
	
	/**
	 * Return font used for labels etc in the mixer window.
	 * @return Mixer label font.
	 */
	public Font getMixerFont() {
		return mixerFont;
	}
	
	public void setUIOperation( UIOperation op ) {
		queueDataEvent( new DataEvent( DataItemID.YADAW_UI_OPERATION, op ));
	}
	
	public Collection<TrackController> getTrackControllers() {
		return trackControllers;
	}
	
	public void addTrackController( TrackController tctrl ) {
		trackControllers.add( tctrl );
		queueDataEvent( new DataEvent( DataItemID.YADAW_TRACKCONTROLLER_ADD, tctrl ));
	}
	
	public void mixerMouseClick( MouseEvent e, Object panelObject ) {
		Class<?> classObj = panelObject.getClass();
		System.out.println( "DataModel mixerMouseClick Type:  " + classObj.getName());
		if( panelObject instanceof InputLabel ) {
			InputLabel ilbl =(InputLabel)panelObject;
			for( TrackController c : trackControllers ) {
				if( ilbl == c.getPanel().getInputPanel().getInputLabel() ) {
					System.out.println( "Track panel " + c.getTrackName() + " InputLabel clicked");
					c.inputLabelClick();
				}
			}
		}
	}
	
	public void addUpdateListener( DataItemID id, DataModelUpdateListenerIf listener ) {
		if( dataListeners == null ) {
			dataListeners = new Vector<DataListenerItem>();
		}
		dataListeners.add( new DataListenerItem( id, listener ));
	}
	
	public void deleteUpdateListener( DataItemID id, DataModelUpdateListenerIf listener ) {
		for( DataListenerItem i : dataListeners ) {
			if( ( DataItemID )i.getID() == id && i.getListener() == listener ) {
				dataListeners.remove( i );
			}
		}
	}
	
	private void queueDataEvent( DataEvent dataEvent ) {
		if (dataListeners != null) {
			System.out.println( "Queueing data event id: " + dataEvent.getID().toString() );
			synchronized( dataUpdateThread ) {
				eventQueue.add(dataEvent);
				dataUpdateThread.notify();
			}
		}
	}
}
