package com.blotty.core.models;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.LongField;

public class RowBuilder {
	
	private Row row;
	private final DataModel dataModel;
	private final ColumnsModel columnsModel;
	
	RowBuilder( DataModel dataModel, ColumnsModel columnsModel ) {
		this.dataModel = dataModel;
		this.columnsModel = columnsModel;
	}
	
	public RowBuilder newRow() throws RowsTypeException {
		this.row = new Row(columnsModel);
		if ( columnsModel.hasAutoKey() ) {
			set(columnsModel.getKey(), LongField.of( columnsModel.getAutoKeyValue()));
		}
		return this;
	}
	
	public RowBuilder set( String[] values ) throws RowsTypeException {
		int shiftIdx = columnsModel.hasAutoKey()?1:0;

		for (int i=0; i<values.length; i++) {
			this.row.set(columnsModel.getColumn(i+shiftIdx), values[i]);
		}		
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