/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.indexes.AbstractIndex;
import com.blotty.core.rows.Row;
import com.blotty.core.rows.Row.RowChange;
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
	
	private void updateIndexesAndStats(Row ...rows) {
		for ( Row r : rows ) {
			for ( AbstractIndex<?> idx : indexes ) {
				idx.insert(r);
			}
			for ( int i=0; i<getColumnsModel().getColumnsCount(); i++) {
				Column col = getColumnsModel().getColumn(i);
				if (col.isEnabledStats()) {
					IGenericField<?> oldValue = r.getFields()[i];
					col.getStatistics().add(oldValue);
				}
			}
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
	
	public RowChange updRow(Row r, boolean trackChanges) throws RowsModelException {
		RowChange changes = rowsModel.updRow(r, trackChanges);
		
		if (changes!=null) {
			Row storedRow = rowsModel.getRow(rowsModel.getKey(r));
			for ( AbstractIndex<?> idx : indexes ) {
				idx.update(storedRow, changes);	
			}
			
			BitSet modCols = changes.modifiedCols();			
			for (int colId = modCols.nextSetBit(0); colId >= 0; colId = modCols.nextSetBit(colId+1)) {
				Column col = getColumnsModel().getColumn(colId);
				if (col.isEnabledStats()) {
					IGenericField<?> oldValue = changes.oldValues().get(changes.resolveId(colId));
					col.getStatistics().upd(storedRow.getFields()[colId], oldValue);
				}
			}
		}
		return changes;
	}
	
	public AbstractDataModel delRow(Row r) throws RowsModelException {
		rowsModel.delRow(r);
		for ( AbstractIndex<?> idx : indexes ) {
			idx.delete(r);
		}		
		for ( int i=0; i<getColumnsModel().getColumnsCount(); i++) {
			Column col = getColumnsModel().getColumn(i);
			if (col.isEnabledStats()) {
				IGenericField<?> oldValue = r.getFields()[i];
				col.getStatistics().del(oldValue);
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
	
	public AbstractRowsModel getRowsModel() {
		return rowsModel;
	}

	public ColumnsModel getColumnsModel() {
		return columnsModel;
	}
	
	public String getId() {
		return id;
	}
	
	public void addIndex(AbstractIndex<?> i) throws RowsModelException, FilterExpressionException {
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
				for ( IGenericField<?> f : r.getFields() ) {
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
