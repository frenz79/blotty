package com.blotty.core.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.blotty.core.commons.exceptions.RowsModelException;

public class InMemoryRowsModel implements IRowsModel {

	private final ColumnsModel columnsModel;
	
	private final Map<String,Row> rowsByKey = new HashMap<>();
		
	public InMemoryRowsModel(ColumnsModel columnsModel) {
		this.columnsModel = columnsModel;
	}

	@Override
	public IRowsModel addRow( Row row ) throws RowsModelException {		
		Row prev = this.rowsByKey.put(row.getKey(), row);
		if ( prev!=null ) {
			throw new RowsModelException(String.format("Duplicated key:%s", row.getKey()));
		}
		return this;
	}

	@Override
	public int getRowsCount() {
		return rowsByKey.size();
	}

	@Override
	public Collection<Row> getAllRows() {
		return rowsByKey.values();
	}
}
