package com.blotty.core.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.RowEvent.RowAction;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.sorters.SorterConditionChain;

public class DataModel extends AbstractDataModel {
	
	private Map<String,IListener<RowEvent>> rowsEventListeners = new ConcurrentHashMap<>();
	
	public DataModel( AbstractRowsModel rowsModel ) {
		super(rowsModel);
	}
	
	public DataModel( String id, AbstractRowsModel rowsModel ) {
		super(id, rowsModel);
	}
		
	public DataModelView createView( String viewId, FilterExpression filter ) throws RowsModelException, FilterExpressionException {
		DataModelView view = new DataModelView( viewId, filter, this );
		return view;
	}

	public DataModelView createView( String viewId, SorterConditionChain sorter ) throws RowsModelException, FilterExpressionException {
		DataModelView view = new DataModelView( viewId, sorter, this );
		return view;
	}
	
	public DataModelView createView( String viewId, FilterExpression filter, SorterConditionChain sorter ) throws RowsModelException, FilterExpressionException {
		DataModelView view = new DataModelView( viewId, filter, sorter, this );
		return view;
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
		for (Row r : rows) {
			fireDataModelChanged( new RowEvent(RowAction.ADD, r) );
		}
		return this;
	}	
	
	private void fireDataModelChanged( RowEvent event ) {
		for( IListener<RowEvent> l : this.rowsEventListeners.values() ) {
			l.onEvent(event);
		}
	}
	
	public void addRowsListener( String id, IListener<RowEvent> listener ) {
		this.rowsEventListeners.put(id, listener);
	}

	public void delRowsListener( String id ) {
		this.rowsEventListeners.remove(id);
	}
}
