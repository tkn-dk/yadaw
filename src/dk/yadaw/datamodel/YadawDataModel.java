package dk.yadaw.datamodel;

import java.util.Collection;
import java.util.Vector;

import dk.yadaw.audio.Asio;

/**
 * Holds all data model objects.
 * @author tkn
 *
 */
public class YadawDataModel {
	
	/**
	 * Enumerations giving IDs to all data objects in model that can be be listened to.
	 * @author tkn
	 *
	 */
	public enum DataID {
		YADAW_ALL,
		YADAW_ASIO_DRIVER_NAME
	};
	
	private Asio asio;
	private String asioDriverName;
	private Collection<DataListenerItem> dataListeners;
	
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
		if (dataListeners != null) {
			for (DataListenerItem i : dataListeners) {
				DataID did = (DataID) i.getId();
				if ( did == DataID.YADAW_ASIO_DRIVER_NAME || did == DataID.YADAW_ALL ) {
					i.getListener().dataItemUpdated(DataID.YADAW_ASIO_DRIVER_NAME, this.asioDriverName);
				}
			}
		}
	}
	
	/**
	 * Returns the name of the used asio driver.
	 * @return Asio driver name.
	 */
	public String getAsioDriverName() {
		return asioDriverName;
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
}
