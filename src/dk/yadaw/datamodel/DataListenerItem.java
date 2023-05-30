package dk.yadaw.datamodel;

public class DataListenerItem {
	private DataModelUpdateListenerIf listener;
	private DataItemID id;

	public DataListenerItem(DataItemID id, DataModelUpdateListenerIf listener) {
		this.listener = listener;
		this.id = id;
	}

	public DataItemID getID() {
		return id;
	}

	public DataModelUpdateListenerIf getListener() {
		return listener;
	}

}
