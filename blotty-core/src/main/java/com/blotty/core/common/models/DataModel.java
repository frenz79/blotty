package com.blotty.core.common.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.models.RowEvent.RowAction;
import com.blotty.core.common.models.commons.Listener;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.types.AbstractDataModel;

public class DataModel extends AbstractDataModel {
	
	private Map<String,Listener<RowEvent>> rowsEventListeners = new ConcurrentHashMap<>();
	
	public DataModel( ColumnsModel columnsModel ) {
		super(columnsModel);
	}
	
	public DataModel( String id, ColumnsModel columnsModel ) {
		super(id, columnsModel);
	}
	
	public RowBuilder getRowBuilder() {
		return new RowBuilder( this, getColumnsModel() );
	}
	
	public DataModelView createView( String viewId, FilterExpression filter ) throws RowsModelException {
		DataModelView view = new DataModelView( viewId, filter, this );
		return view;
	}

	@Override
	public AbstractDataModel addRow(Row r) throws RowsModelException {
		super.addRow(r);
		fireDataModelChanged( new RowEvent(RowAction.ADD, r) );
		return this;
	}
	
	private void fireDataModelChanged( RowEvent event ) {
		for( Listener<RowEvent> l : this.rowsEventListeners.values() ) {
			l.onEvent(event);
		}
	}	
	
	public void addRowsListener( String id, Listener<RowEvent> listener ) {
		this.rowsEventListeners.put(id, listener);
	}

	public void delRowsListener( String id ) {
		this.rowsEventListeners.remove(id);
	}
}
