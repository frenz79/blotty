package com.blotty.core.models;

import java.util.UUID;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public abstract class AbstractDataModel {

	private final String id;
	private final ColumnsModel columnsModel;
	private IRowsModel rowsModel;
	
	public AbstractDataModel( String id, ColumnsModel columnsModel ) {
		this.id = id;
		this.columnsModel = columnsModel;
		this.rowsModel = new InMemoryRowsModel(columnsModel);
		/*
		try {
			this.rowsModel = new LOBRowsModel(columnsModel);
		} catch (Exception e) {
			this.rowsModel = new InMemoryRowsModel(columnsModel);
			e.printStackTrace();
		}
		*/
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
	
	public void stream( IConsumer<Row> c ) throws RowsModelException {
		for ( Row r : rowsModel.getAllRows() ) {
			if (!c.stream(r)) {
				break;
			}
		}
	}
	
	protected IRowsModel getRowsModel() {
		return rowsModel;
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
			for ( IGenericField f : r.getFields() ) {
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
