package com.blotty.core.models;

public class RowEvent {

	public static enum RowAction {
		ADD,
		UPD,
		DEL		
	}
	
	private final RowAction action;
	private final Row row;
	private final long eventTime = System.currentTimeMillis();
	
	public RowEvent(RowAction action, Row row) {
		super();
		this.action = action;
		this.row = row;
	}

	public RowAction getAction() {
		return action;
	}

	public Row getRow() {
		return row;
	}

	public long getEventTime() {
		return eventTime;
	}
}
