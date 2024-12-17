package com.blotty.core.models;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.FilterExpression;

public class DataModelView extends AbstractDataModel implements IListener<RowEvent> {

	private final FilterExpression filter;
	private final DataModel parentDataModel;
	
	public DataModelView(String id, FilterExpression filter, DataModel parentDataModel) throws RowsModelException {
		super(id, parentDataModel.getColumnsModel());
		this.filter = filter;
		this.parentDataModel = parentDataModel;
		populate();
		
		this.parentDataModel.addRowsListener(id, this);
	}

	private void populate() throws RowsModelException {
		parentDataModel.stream( new IConsumer<Row>() {
			@Override
			public boolean stream(Row row) throws RowsModelException {
				if ( filter.apply(row) ) {
					addRow( row );
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
					addRow( row );
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

	public DataModel getParentDataModel() {
		return parentDataModel;
	}
}
