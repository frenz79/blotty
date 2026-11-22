package com.blotty.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public abstract class AbstractDataModel {

	private final String id;
	private final ColumnsModel columnsModel;
	private final AbstractRowsModel rowsModel;
	private final List<SearchIndex> indexes;
	
	public AbstractDataModel( String id, AbstractRowsModel rowsModel ) {
		this.id = id;
		this.columnsModel = rowsModel.getColumnsModel();
		this.rowsModel = rowsModel;
		this.indexes = new ArrayList<>();
	}
	
	public AbstractDataModel( AbstractRowsModel rowsModel ) {
		this(UUID.randomUUID().toString(), rowsModel);
	}
	
	public Row getRow(int i) {
		return rowsModel.getRow(i);
	}
	
	public AbstractDataModel updRow(Row r) throws RowsModelException {
		rowsModel.updRow(r);
		updateIndexes(r);
		return this;
	}
	
	public AbstractDataModel addRow(Row r) throws RowsModelException {
		rowsModel.addRow(r);
		updateIndexes(r);
		return this;
	}
	
	public AbstractDataModel addRows(Row ...r) throws RowsModelException {
		rowsModel.addRows(r);
		updateIndexes(r);
		return this;
	}

	public AbstractDataModel addRow(Row.Builder r) throws RowsModelException {
		return addRow(r.build());
	}
	
	public int getRowsCount() {
		return rowsModel.getRowsCount();
	}
	
	public void stream( IConsumer<Row> c ) throws RowsModelException, FilterExpressionException {
		rowsModel.stream(c);
	}
	
	protected AbstractRowsModel getRowsModel() {
		return rowsModel;
	}

	public ColumnsModel getColumnsModel() {
		return columnsModel;
	}
	
	public String getId() {
		return id;
	}
	
	public void addIndex(SearchIndex i) throws RowsModelException, FilterExpressionException {
		this.indexes.add(i);
		i.getColumn().setSearchIndex(i);
		i.build(rowsModel);
	}
	
	private void updateIndexes(Row ...rows) {
		for ( Row r : rows ) {
			for ( SearchIndex idx : indexes ) {
				idx.update(r);
			}
		}
	}
	
	public List<SearchIndex> getIndexes() {
		return indexes;
	}
	
	public boolean hasIndexes() {
		return !indexes.isEmpty();
	}
	
	public String dumpToString() throws RowsModelException, FilterExpressionException {
		StringBuilder ret = new StringBuilder("Model name:").append(getId()).append("\n");
		
		AtomicInteger colsSize = new AtomicInteger();
		for ( Column c : columnsModel.getColumns() ) {
			if ( c.getName().length()>colsSize.get() ) {
				colsSize.set( c.getName().length() );
			}
		}
		
		ret.append("|");
		for ( Column c : columnsModel.getColumns() ) {
			ret.append(c.getName());
			for ( int i=c.getName().length(); i<colsSize.get(); i++ ) {
				ret.append(" ");
			}
			ret.append("|");
		}
		
		ret.append("\n");
		
		for ( int i=0; i<colsSize.get()*columnsModel.getColumnsCount()+columnsModel.getColumnsCount()+1; i++ ) {
			ret.append("-");
		}
		
		ret.append("\n");
		rowsModel.stream(new IConsumer<Row>() {
			
			@Override
			public boolean stream(Row r) throws RowsModelException {
				ret.append("|");
				for ( IGenericField f : r.getFields() ) {
					String str = f.toString();
					ret.append(str);
					for ( int i=str.length(); i<colsSize.get(); i++ ) {
						ret.append(" ");
					}
					ret.append("|");
				}
				ret.append("\n");
				return true;
			}
		});
		return ret.toString();
	}
}
