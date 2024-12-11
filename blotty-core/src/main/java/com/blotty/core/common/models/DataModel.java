package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.models.commons.Consumer;

public class DataModel {

	private final ColumnsModel columnsModel;
	private final RowsModel rowsModel;
	
	public DataModel( ColumnsModel columnsModel ) {
		this.columnsModel = columnsModel;
		this.rowsModel = new RowsModel( columnsModel );
	}
	
	public RowsBuilder getRowsBuilder() {
		return new RowsBuilder();
	}
	
	public static class RowsBuilder {
		
		private Row row;
				
		public Row build() {
			return row;
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
}
