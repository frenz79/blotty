/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core;

import java.util.Comparator;

import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.rows.Row;
import com.blotty.core.rows.Row.RowChange;
import com.blotty.core.types.IGenericField;

public abstract class AbstractRowsModel {

	private final ColumnsModel columnsModel;
	
	public abstract void delRow(Row row) throws RowsModelException;
	public abstract RowChange updRow(Row row, boolean trackChanges) throws RowsModelException;
	public abstract void addRow(Row row) throws RowsModelException;
	public abstract void addRows(Row ...row) throws RowsModelException;
	public abstract void stream(IConsumer<Row> c) throws RowsModelException, FilterExpressionException;
	public abstract void sort(Comparator<Row> c) throws RowsModelException;
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
}