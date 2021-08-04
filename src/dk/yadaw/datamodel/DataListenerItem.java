package dk.yadaw.datamodel;

public class DataListenerItem {
	private DataModelUpdateListenerIf listener;
	private Object id;

	public DataListenerItem(Object id, DataModelUpdateListenerIf listener) {
		this.listener = listener;
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public DataModelUpdateListenerIf getListener() {
		return listener;
	}

}
