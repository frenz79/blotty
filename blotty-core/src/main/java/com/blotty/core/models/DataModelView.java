package com.blotty.core.models;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.types.IGenericField;

public class DataModelView extends AbstractDataModel implements IListener<RowEvent> {

	private final FilterExpression filter;
	private final DataModel parentDataModel;
	private SorterConditionChain sorter;
	
	public DataModelView(String id, FilterExpression filter, DataModel parentDataModel) throws RowsModelException {
		this(id, filter, null, parentDataModel);
	}

	public DataModelView(String id, SorterConditionChain sorter, DataModel parentDataModel) throws RowsModelException {
		this(id, null, sorter, parentDataModel);
	}
	
	public DataModelView(String id, FilterExpression filter, SorterConditionChain sorter, DataModel parentDataModel) throws RowsModelException {
		super(id, parentDataModel.getColumnsModel());
		this.filter = filter;
		this.sorter = sorter;
		this.parentDataModel = parentDataModel;
		populate();
		
		if ( sorter!=null ) {
			sort( sorter );
		}
		
		this.parentDataModel.addRowsListener(id, this);
	}
	
	private void populate() throws RowsModelException {
		parentDataModel.stream( new IConsumer<Row>() {
			@Override
			public boolean stream(Row row) throws RowsModelException {
				if ( filter==null || filter.apply(row) ) {
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
				if ( filter==null || filter.apply(row) ) {
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
	
	public void sort( SorterConditionChain sorter ) {
		this.sorter = sorter;
		this.sorter.apply( getRowsModel() );
	}
	
	public Row getRow( int position ) {
		return getRowsModel().getRow( position );
	}
	
	public Row getRow( IGenericField key ) {
		return getRowsModel().getRow( key );
	}
}
