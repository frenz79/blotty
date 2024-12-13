package com.blotty.core.common.models.types;

import java.util.UUID;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.ColumnsModel;
import com.blotty.core.common.models.Row;
import com.blotty.core.common.models.RowsModel;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.commons.GenericField;

public abstract class AbstractDataModel {

	private final String id;
	private final ColumnsModel columnsModel;
	private final RowsModel rowsModel;
	
	public AbstractDataModel( String id, ColumnsModel columnsModel ) {
		this.id = id;
		this.columnsModel = columnsModel;
		this.rowsModel = new RowsModel( columnsModel );
	}
	
	public AbstractDataModel( ColumnsModel columnsModel ) {
		this(UUID.randomUUID().toString(), columnsModel);
	}
	
	public AbstractDataModel addRow(Row r) throws RowsModelException {
		rowsModel.addRow(r);
		return this;
	}

	public int getRowsCount() {
		return rowsModel.getRowsCount();
	}
	
	public void stream( Consumer<Row> c ) throws RowsModelException {
		for ( Row r : rowsModel.getAllRows() ) {
			if (!c.stream(r)) {
				break;
			}
		}
	}

	public ColumnsModel getColumnsModel() {
		return columnsModel;
	}
	
	public String getId() {
		return id;
	}
	
	public String dumpToString() {
		StringBuilder ret = new StringBuilder("Model name:").append(getId()).append("\n");
		
		int colsSize = 0;
		for ( Column c : columnsModel.getColumns() ) {
			if ( c.getName().length()>colsSize ) {
				colsSize = c.getName().length();
			}
		}
		
		ret.append("|");
		for ( Column c : columnsModel.getColumns() ) {
			ret.append(c.getName());
			for ( int i=c.getName().length(); i<colsSize; i++ ) {
				ret.append(" ");
			}
			ret.append("|");
		}
		
		ret.append("\n");
		
		for ( int i=0; i<colsSize*columnsModel.getColumnsCount()+columnsModel.getColumnsCount()+1; i++ ) {
			ret.append("-");
		}
		
		ret.append("\n");
		
		for ( Row r : rowsModel.getAllRows() ) {
			ret.append("|");
			for ( GenericField f : r.getFields() ) {
				String str = f.toString();
				ret.append(str);
				for ( int i=str.length(); i<colsSize; i++ ) {
					ret.append(" ");
				}
				ret.append("|");
			}
			ret.append("\n");
		}
		return ret.toString();
	}
}
