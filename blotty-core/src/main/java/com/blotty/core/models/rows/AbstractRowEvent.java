package com.blotty.core.models.rows;

public abstract class AbstractRowEvent {

	public static enum RowAction {
		ADD,
		UPD,
		DEL		
	}

	private final RowAction action;
	private final long eventTime = System.currentTimeMillis();
	
	public AbstractRowEvent(RowAction action) {
		super();
		this.action = action;
	}

	public RowAction getAction() {
		return action;
	}

	public long getEventTime() {
		return eventTime;
	}
	
	
}
