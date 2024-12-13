package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsModelException;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.commons.Listener;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;

public class DataModelView implements Listener<RowEvent> {

	private final String id;
	private final FilterExpression filter;
	private final DataModel parentDataModel;
	private final RowsModel rowsModel;
	
	public DataModelView(String id, FilterExpression filter, DataModel parentDataModel) throws RowsModelException {
		super();
		this.id = id;
		this.parentDataModel = parentDataModel;
		this.filter = filter;
		this.rowsModel = new RowsModel( parentDataModel.getColumnsModel() );
		populate();
		
		this.parentDataModel.addRowsListener(id, this);
	}

	private void populate() throws RowsModelException {
		parentDataModel.stream( new Consumer<Row>() {
			@Override
			public boolean stream(Row row) throws RowsModelException {
				if ( filter.apply(row) ) {
					rowsModel.addRow( row );
				}
				return true;
			}			
		});
	}

	@Override
	public void onEvent(RowEvent event) {
		try {
			Row row = event.getRow();
			switch( event.getAction() ) {
			case ADD:
				if ( filter.apply(row) ) {
					rowsModel.addRow( row );
				}
				break;
			case DEL:
				// TODO: code me
				break;
			case UPD:
				// NOP
				break;
			default:
				break;
			}
		} catch ( Exception ex ) {
			throw new RuntimeException(ex);
		}
	}
	
	public String getId() {
		return id;
	}
	
	public DataModel getParentDataModel() {
		return parentDataModel;
	}

	public int getRowsCount() {
		return rowsModel.getRowsCount();
	}
	
	public String dumpToString() {
		StringBuilder ret = new StringBuilder();
		int colsSize = 0;
		for ( Column c : parentDataModel.getColumnsModel().getColumns() ) {
			if ( c.getName().length()>colsSize ) {
				colsSize = c.getName().length();
			}
		}
		
		for ( Column c : parentDataModel.getColumnsModel().getColumns() ) {
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
