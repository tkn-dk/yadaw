package dk.yadaw.datamodel;

/**
 * Listner interface for data model updates.
 * @author tkn
 *
 */
public interface DataModelUpdateListenerIf {
	public void dataItemUpdated( Object itemID, Object itemData );
}
