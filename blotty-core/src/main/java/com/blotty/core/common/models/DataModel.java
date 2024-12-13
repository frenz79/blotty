package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.commons.GenericField;

public class DataModel {

	private final ColumnsModel columnsModel;
	private final RowsModel rowsModel;
	
	public DataModel( ColumnsModel columnsModel ) {
		this.columnsModel = columnsModel;
		this.rowsModel = new RowsModel( columnsModel );
	}
	
	public RowBuilder getRowBuilder() {
		return new RowBuilder( columnsModel );
	}
	
	public class RowBuilder {
		
		private Row row;
		private final ColumnsModel columnsModel;
		
		public RowBuilder( ColumnsModel columnsModel ) {
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
			addRow( row );
			return this;
		}		
	}

	public DataModel addRow(Row r) throws RowsModelException {
		rowsModel.addRow(r);
		return this;
	}

	public int getRowsCount() {
		return rowsModel.getRowsCount();
	}
	
	public void stream( Consumer<Row> c ) {
		for ( Row r : rowsModel.getAllRows() ) {
			if (!c.stream(r)) {
				break;
			}
		}
	}

	public ColumnsModel getColumnsModel() {
		return columnsModel;
	}
}
