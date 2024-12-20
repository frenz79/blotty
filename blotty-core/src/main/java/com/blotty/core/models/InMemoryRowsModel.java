package com.blotty.core.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public class InMemoryRowsModel implements IRowsModel {

	private final ColumnsModel columnsModel;
	
	private final Map<IGenericField,Row> rowsByKey = new HashMap<>();
	private final List<Row> rows = new ArrayList<>();
		
	public InMemoryRowsModel(ColumnsModel columnsModel) {
		this.columnsModel = columnsModel;
	}

	@Override
	public IRowsModel addRow( Row row ) throws RowsModelException {		
		Row prev = this.rowsByKey.put(row.getFields()[ columnsModel.getKey().getId() ], row);
		if ( prev!=null ) {
			throw new RowsModelException(String.format("Duplicated key:%s", columnsModel.getKey()));
		}
		this.rows.add(row);
		return this;
	}

	@Override
	public int getRowsCount() {
		return rows.size();
	}

	@Override
	public List<Row> getAllRows() {
		return rows;
	}

	@Override
	public Row getRow(int position) {
		return rows.get(position);
	}

	@Override
	public Row getRow(IGenericField key) {
		return rowsByKey.get(key);
	}
}
