package com.blotty.core.models;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;

public class RowBuilder {
	
	private Row row;
	private final DataModel dataModel;
	private final ColumnsModel columnsModel;
	
	RowBuilder( DataModel dataModel, ColumnsModel columnsModel ) {
		this.dataModel = dataModel;
		this.columnsModel = columnsModel;
	}
	
	public RowBuilder newRow() {
		this.row = new Row(columnsModel);
		return this;
	}
	
	public RowBuilder set( Column col, IGenericField fVal ) throws RowsTypeException {
		this.row.set(col, fVal);
		return this;
	}
	
	public RowBuilder set( Column col, String val ) throws RowsTypeException {
		this.row.set(col, val);
		return this;
	}
	
	public RowBuilder set( String colName, IGenericField fVal ) throws RowsTypeException {
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