package dk.yadaw.datamodel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.Mixer;

/**
 * Holds all data model objects.
 * @author tkn
 *
 */
public class YadawDataModel {
	private final int numSends = 2;
	private Asio asio;
	private String asioDriverName;
	private Collection<DataListenerItem> dataListeners;
	private Mixer mixer;
	private Thread dataUpdateThread;
	private LinkedList<DataEvent>eventQueue;
	
		
	/**
	 * Constructs data model object.
	 */
	public YadawDataModel() {
		eventQueue = new LinkedList<DataEvent>();
		dataUpdateThread = new Thread() {

			@Override
			public void run() {
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
		queueDataEvent( new DataEvent( DataItemID.YADAW_ASIO, this.asio ));
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
