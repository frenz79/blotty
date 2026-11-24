package com.blotty.core.models;

import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.models.query.QueryPlan;
import com.blotty.core.models.query.QueryPlanner;
import com.blotty.core.models.rows.AbstractRowEvent;
import com.blotty.core.models.rows.Row;
import com.blotty.core.models.rows.RowEvent;
import com.blotty.core.models.rows.RowEventBatch;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.types.IGenericField;

public class DataModelView extends AbstractDataModel implements IListener<AbstractRowEvent> {

	private final QueryDefinition queryDef;
	private final DataModel parentDataModel;
	private final QueryPlanner queryPlanner;
	
	public DataModelView(String id, QueryDefinition queryDef, DataModel parentDataModel) throws RowsModelException, FilterExpressionException {
		super(id, new InMemoryRowsModel(parentDataModel.getColumnsModel()));
		this.queryDef = queryDef;
		this.parentDataModel = parentDataModel;
		this.queryPlanner = new QueryPlanner();
		populate();
		this.parentDataModel.addRowsListener(id, this);
	}

	private void populate() throws RowsModelException, FilterExpressionException {
		QueryPlan queryPlan = queryPlanner.plan(queryDef, parentDataModel);
		
		for ( Row row : queryPlan.getCandidateRows().getRows() ) {
			IFilterCondition filter = queryPlan.getResidualFilter();
			if (filter==null || filter.apply(row)) {
				addRow( row );
			}
		}
				
		if ( queryDef.hasSorter() ) {
			getRowsModel().sort(queryDef.getSorter());
		}
	}

	@Override
	public void onEvent(AbstractRowEvent event) {
		try {
			if (event instanceof RowEventBatch batch) {
				handleEvent(batch);
			} else if (event instanceof RowEvent evt) {
				handleEvent(evt);
			}
		} catch ( Exception ex ) {
			throw new RuntimeException(ex);
		}
	}

	private void handleEvent(RowEvent event) throws FilterExpressionException, RowsModelException {
		Row row = event.getRow();
		FilterExpression filter = queryDef.getFilter();
		SorterConditionChain sorter = queryDef.getSorter();
		switch( event.getAction() ) {
		case ADD:
			if (filter == null || filter.apply(row)) {
				addRow(row);
				if (sorter != null) {
					getRowsModel().sort(sorter);
				}
			}
			break;
		case UPD:
			if (filter == null || filter.apply(row)) {
				updRow(row, event.getOldRow());
				if (sorter != null) {
					getRowsModel().sort(sorter);
				}
			}				
			break;
		case DEL:
			getRowsModel().delRow(row);
			break;			
		default:
			break;
		}
	}

	private void handleEvent(RowEventBatch batch) {
		// TODO Auto-generated method stub
	}

	public DataModel getParentDataModel() {
		return parentDataModel;
	}

	public void sort(SorterConditionChain sorter) {
		this.queryDef.setSorter( sorter );
		try {
			getRowsModel().sort(sorter);
		} catch (RowsModelException e) {
			throw new RuntimeException("Error during sorting", e);
		}
	}

	public Row getRow( int position ) {
		return getRowsModel().getRow( position );
	}

	public Row getRow( IGenericField key ) {
		return getRowsModel().getRow( key );
	}
}
