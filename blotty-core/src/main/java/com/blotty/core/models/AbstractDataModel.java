package com.blotty.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
import com.blotty.core.models.indexes.AbstractIndex;
import com.blotty.core.models.indexes.HashIndex;
import com.blotty.core.models.rows.Row;
import com.blotty.core.types.IGenericField;

public abstract class AbstractDataModel {

	private final String id;
	private final ColumnsModel columnsModel;
	private final AbstractRowsModel rowsModel;
	private final List<AbstractIndex<?>> indexes;
	
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
		
	private void updateStats(Row newR, Row oldR) {
		for ( int i=0; i<getColumnsModel().getColumnsCount(); i++) {
			if ( newR.getFields()[i].isNull() ) {
				getColumnsModel().getColumn(i).getStatistics().incNullValues();
			} else {
				getColumnsModel().getColumn(i).getStatistics().incNotNullValues();
			}
			if (oldR!=null) {
				if ( oldR.getFields()[i].isNull() ) {
					getColumnsModel().getColumn(i).getStatistics().incNullValues();
				} else {
					getColumnsModel().getColumn(i).getStatistics().incNotNullValues();
				}
			}
		}
	}

	private void updateIndexesAndStats(Row ...rows) {
		for ( Row r : rows ) {
			for ( AbstractIndex<?> idx : indexes ) {
				idx.insert(r);
			}
			updateStats(r, null);
		}
	}
	
	public AbstractDataModel addRow(Row r) throws RowsModelException {
		rowsModel.addRow(r);
		updateIndexesAndStats(r);
		return this;
	}
	
	public AbstractDataModel addRows(Row ...r) throws RowsModelException {
		rowsModel.addRows(r);
		updateIndexesAndStats(r);
		return this;
	}

	public AbstractDataModel addRow(Row.Builder r) throws RowsModelException {
		return addRow(r.build());
	}
	
	public AbstractDataModel updRow(Row oldR, Row newR) throws RowsModelException {
		rowsModel.updRow(oldR, newR);
		for ( AbstractIndex<?> idx : indexes ) {
			idx.update(oldR, newR);			
			updateStats(newR, oldR);
		}
		return this;
	}
	
	public AbstractDataModel delRow(Row r) throws RowsModelException {
		rowsModel.delRow(r);
		for ( AbstractIndex<?> idx : indexes ) {
			idx.delete(r);			
			for ( int i=0; i<getColumnsModel().getColumnsCount(); i++) {
				if ( r.getFields()[i].isNull() ) {
					getColumnsModel().getColumn(i).getStatistics().decNullValues();
				} else {
					getColumnsModel().getColumn(i).getStatistics().decNotNullValues();
				}
			}
		}
		return this;
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
	
	public void addIndex(HashIndex i) throws RowsModelException, FilterExpressionException {
		this.indexes.add(i);
		i.build(rowsModel);
	}
	
	public List<AbstractIndex<?>> getIndexes() {
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
