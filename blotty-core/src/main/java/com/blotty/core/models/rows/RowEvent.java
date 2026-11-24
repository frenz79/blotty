package com.blotty.core.models.rows;

import com.blotty.core.models.rows.Row.RowChange;

public class RowEvent extends AbstractRowEvent {
	
	private final Row row;
	private final RowChange rowChange;
	
	public RowEvent(RowAction action, Row row) {
		this(action, row, null);
	}
	
	public RowEvent(RowAction action, Row row, RowChange rowChange) {
		super(action);
		this.row = row;
		this.rowChange = rowChange;
	}

	public Row getRow() {
		return row;
	}

	public RowChange getRowChange() {
		return rowChange;
	}
}
