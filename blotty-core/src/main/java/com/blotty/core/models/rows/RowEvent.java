package com.blotty.core.models.rows;

public class RowEvent extends AbstractRowEvent {
	
	private final Row row;
	private final Row oldRow;
	
	public RowEvent(RowAction action, Row row) {
		this(action, row, null);
	}
	
	public RowEvent(RowAction action, Row row, Row oldRow) {
		super(action);
		this.row = row;
		this.oldRow = oldRow;
	}

	public Row getRow() {
		return row;
	}

	public Row getOldRow() {
		return oldRow;
	}
}
