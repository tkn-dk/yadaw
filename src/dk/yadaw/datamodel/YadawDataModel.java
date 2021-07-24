package dk.yadaw.datamodel;

import dk.yadaw.audio.Asio;

/**
 * Holds all data model objects.
 * @author tkn
 *
 */
public class YadawDataModel {
	private Asio asio;
	
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
}
