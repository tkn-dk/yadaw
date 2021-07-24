package dk.yadaw.datamodel;

/**
 * Creates and holds one instance of the system data model.
 * @author tkn
 *
 */
public class DataModelInstance {
	private static YadawDataModel model;
	
	/**
	 * Creates the data model object on first call.
	 * @return Reference to common data model object.
	 */
	public static YadawDataModel getModelInstance() {
		if( model == null ) {
			model = new YadawDataModel();
		}
		return model;
	}

}
