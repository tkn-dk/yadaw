package dk.yadaw.datamodel;

import java.util.Collection;
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

	/**
	 * Enumerations giving IDs to all data objects in model that can be be listened to.
	 * @author tkn
	 *
	 */
	public enum DataID {
		YADAW_ALL,
		YADAW_ASIO_DRIVER_NAME,
		YADAW_MIXER
	};
	
	private Asio asio;
	private String asioDriverName;
	private Collection<DataListenerItem> dataListeners;
	private Mixer mixer;
		
	/**
	 * Constructs data model objects.
	 */
	public YadawDataModel() {
		asio = new Asio();
	}

	/**
	 * Get a reference to Asio object.
	 * @return	Asio object.
	 */
	public Asio getAsio() {
		return asio;
	}
	
	/**
	 * Sets the name of the used asio driver in the model.
	 * @param asioDriverName Asio driver name.
	 */
	public void setAsioDriverName( String asioDriverName ) {
		this.asioDriverName = asioDriverName;
		notifyUpdateListeners( DataID.YADAW_ASIO_DRIVER_NAME, this.asioDriverName );
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
		notifyUpdateListeners( DataID.YADAW_MIXER, this.mixer );
	}
	
	/**
	 * Return number of sends set in the system
	 * @return
	 */
	public int getNumSends() {
		return numSends;
	}
	
	public void addUpdateListener( DataID id, DataModelUpdateListenerIf listener ) {
		if( dataListeners == null ) {
			dataListeners = new Vector<DataListenerItem>();
		}
		dataListeners.add( new DataListenerItem( id, listener ));
	}
	
	public void deleteUpdateListener( DataID id, DataModelUpdateListenerIf listener ) {
		for( DataListenerItem i : dataListeners ) {
			if( ( DataID )i.getId() == id && i.getListener() == listener ) {
				dataListeners.remove( i );
			}
		}
	}
	
	private void notifyUpdateListeners( DataID id, Object dataObject ) {
		if (dataListeners != null) {
			for (DataListenerItem i : dataListeners) {
				DataID did = (DataID) i.getId();
				if ( did == id || did == DataID.YADAW_ALL ) {
					i.getListener().dataItemUpdated(id, dataObject );
				}
			}
		}

	}
}
