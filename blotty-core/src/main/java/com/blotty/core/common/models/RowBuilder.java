package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.commons.GenericField;

public class RowBuilder {
	
	private Row row;
	private final DataModel dataModel;
	private final ColumnsModel columnsModel;
	
	RowBuilder( DataModel dataModel, ColumnsModel columnsModel ) {
		this.dataModel = dataModel;
		this.columnsModel = columnsModel;
	}
	
	public RowBuilder newRow(String key) {
		this.row = new Row(key, columnsModel);
		return this;
	}
	
	public RowBuilder set( String colName, GenericField fVal ) throws RowsTypeException {
		this.row.set(columnsModel.getColumn(colName), fVal);
		return this;
	}
	
	public RowBuilder set( String colName, String val ) throws RowsTypeException {
		this.row.set(columnsModel.getColumn(colName), val);
		return this;
	}
	
	public RowBuilder addToModel() throws RowsModelException {
		dataModel.addRow( row );
		return this;
	}
	
	public DataModel getDataModel() {
		return dataModel;
	}
}