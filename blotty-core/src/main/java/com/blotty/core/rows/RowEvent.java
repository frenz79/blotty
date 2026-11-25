/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.rows;

import com.blotty.core.rows.Row.RowChange;

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
