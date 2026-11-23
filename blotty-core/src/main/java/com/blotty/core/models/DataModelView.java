package com.blotty.core.models;

import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.models.query.QueryPlan;
import com.blotty.core.models.query.QueryPlanner;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.types.IGenericField;

public class DataModelView extends AbstractDataModel implements IListener<RowEvent> {

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
	public void onEvent(RowEvent event) {
		try {
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
					updRow(row);
					if (sorter != null) {
						getRowsModel().sort(sorter);
					}
				}				
				break;
			case DEL:
				// IGenericField key = getColumnsModel().getKey() != null ? getColumnsModel().getKey().fieldOf(row) : null;
				// Se implementi deleteRow() nello storage:
				// getRowsModel().deleteRow(key);
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
