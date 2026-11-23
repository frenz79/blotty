package com.blotty.core.models;

public class RowEvent {

	public static enum RowAction {
		ADD,
		UPD,
		DEL		
	}
	
	private final RowAction action;
	private final Row row;
	private final Row oldRow;
	private final long eventTime = System.currentTimeMillis();
	
	public RowEvent(RowAction action, Row row) {
		this(action, row, null);
	}
	
	public RowEvent(RowAction action, Row row, Row oldRow) {
		super();
		this.action = action;
		this.row = row;
		this.oldRow = oldRow;
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

	public Row getOldRow() {
		return oldRow;
	}
}
