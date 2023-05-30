package dk.yadaw.datamodel;

public class DataEvent {
	private DataItemID id;
	private Object dataItem;
	
	public DataEvent( DataItemID id, Object dataItem ) {
		this.id = id;
		this.dataItem = dataItem;
	}
	
	public DataItemID getID() {
		return id;
	}
	
	public Object getDataItem() {
		return dataItem;
	}

}
