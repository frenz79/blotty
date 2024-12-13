package com.blotty.core.common.models;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.models.RowEvent.RowAction;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.commons.Listener;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;

public class DataModel {

	private final String id;
	private final ColumnsModel columnsModel;
	private final RowsModel rowsModel;
	
	private Map<String,Listener<RowEvent>> rowsEventListeners = new ConcurrentHashMap<>();
	
	public DataModel( ColumnsModel columnsModel ) {
		this(UUID.randomUUID().toString(), columnsModel);
	}
	
	public DataModel( String id, ColumnsModel columnsModel ) {
		this.id = id;
		this.columnsModel = columnsModel;
		this.rowsModel = new RowsModel( columnsModel );
	}
	
	public RowBuilder getRowBuilder() {
		return new RowBuilder( this, columnsModel );
	}

	public DataModel addRow(Row r) throws RowsModelException {
		rowsModel.addRow(r);
		fireDataModelChanged( new RowEvent(RowAction.ADD, r) );
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
	
	public DataModelView createView( String viewId, FilterExpression filter ) throws RowsModelException {
		DataModelView view = new DataModelView( viewId, filter, this );
		return view;
	}

	private void fireDataModelChanged( RowEvent event ) {
		for( Listener<RowEvent> l : this.rowsEventListeners.values() ) {
			l.onEvent(event);
		}
	}	
	
	public String getId() {
		return id;
	}
	
	public void addRowsListener( String id, Listener<RowEvent> listener ) {
		this.rowsEventListeners.put(id, listener);
	}

	public void delRowsListener( String id ) {
		this.rowsEventListeners.remove(id);
	}

	public String dumpToString() {
		StringBuilder ret = new StringBuilder();
		int colsSize = 0;
		for ( Column c : columnsModel.getColumns() ) {
			if ( c.getName().length()>colsSize ) {
				colsSize = c.getName().length();
			}
		}
		
		for ( Column c : columnsModel.getColumns() ) {
			ret.append("|").append(c.getName());
			for ( int i=c.getName().length(); i<colsSize; i++ ) {
				ret.append(" ");
			}
			ret.append("|");
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
