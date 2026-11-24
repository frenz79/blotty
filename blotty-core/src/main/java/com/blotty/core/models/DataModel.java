/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.models.rows.AbstractRowEvent;
import com.blotty.core.models.rows.AbstractRowEvent.RowAction;
import com.blotty.core.models.rows.Row;
import com.blotty.core.models.rows.RowEvent;
import com.blotty.core.models.rows.RowEventBatch;

public class DataModel extends AbstractDataModel {
	
	private Map<String,IListener<AbstractRowEvent>> rowsEventListeners = new ConcurrentHashMap<>();
	
	public DataModel( AbstractRowsModel rowsModel ) {
		super(rowsModel);
	}
	
	public DataModel( String id, AbstractRowsModel rowsModel ) {
		super(id, rowsModel);
	}
		
	public DataModelView createView( String viewId, QueryDefinition queryDef ) throws RowsModelException, FilterExpressionException {
		DataModelView view = new DataModelView( viewId, queryDef, this );
		return view;
	}
	
	@Override
	public AbstractDataModel delRow(Row r) throws RowsModelException {
		super.delRow(r);
		fireDataModelChanged( new RowEvent(RowAction.DEL, r) );
		return this;
	}
	
	@Override
	public AbstractDataModel updRow(Row r, boolean trackChanges) throws RowsModelException {
		super.updRow(r, trackChanges);
		fireDataModelChanged( new RowEvent(RowAction.UPD, r) );
		return this;
	}
	
	@Override
	public AbstractDataModel addRow(Row r) throws RowsModelException {
		super.addRow(r);
		fireDataModelChanged( new RowEvent(RowAction.ADD, r) );
		return this;
	}
	
	@Override
	public AbstractDataModel addRows(Row ...rows) throws RowsModelException {
		super.addRows(rows);
		fireDataModelChanged( new RowEventBatch(RowAction.ADD) );
		return this;
	}	
	
	private void fireDataModelChanged( AbstractRowEvent event ) {
		for( IListener<AbstractRowEvent> l : this.rowsEventListeners.values() ) {
			l.onEvent(event);
		}
	}
	
	public void addRowsListener( String id, IListener<AbstractRowEvent> listener ) {
		this.rowsEventListeners.put(id, listener);
	}

	public void delRowsListener( String id ) {
		this.rowsEventListeners.remove(id);
	}
}
