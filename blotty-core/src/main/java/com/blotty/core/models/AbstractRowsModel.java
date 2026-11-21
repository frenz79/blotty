package com.blotty.core.models;

import java.util.Comparator;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public abstract class AbstractRowsModel {

	private final ColumnsModel columnsModel;
	
	public abstract AbstractRowsModel addRow(Row row) throws RowsModelException;
	public abstract AbstractRowsModel addRows(Row ...row) throws RowsModelException;
	
	public abstract int getRowsCount();

	public abstract Row getRow(int position);

	public abstract Row getRow(IGenericField key);

	public AbstractRowsModel(ColumnsModel columnsModel) {
		super();
		this.columnsModel = columnsModel;
	}

	public ColumnsModel getColumnsModel() {
		return columnsModel;
	}
	
	public IGenericField getKey( Row row ) {
		return row.getFields()[ columnsModel.getKey().getId() ];
	}

	public abstract void stream(IConsumer<Row> c) throws RowsModelException;

	public abstract void sort(Comparator<Row> c) throws RowsModelException;
	
}