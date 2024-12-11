package com.blotty.core.common.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.blotty.core.common.exceptions.RowsModelException;

public class RowsModel {

	private final ColumnsModel columnsModel;
	
	private final Map<String,Row> rowsByKey = new HashMap<>();
		
	public RowsModel(ColumnsModel columnsModel) {
		this.columnsModel = columnsModel;
	}

	public RowsModel addRow( Row row ) throws RowsModelException {		
		Row prev = this.rowsByKey.put(row.getKey(), row);
		if ( prev!=null ) {
			throw new RowsModelException(String.format("Duplicated key:%s", row.getKey()));
		}
		return this;
	}

	public int getRowsCount() {
		return rowsByKey.size();
	}

	public Collection<Row> getAllRows() {
		return rowsByKey.values();
	}
}
