package com.blotty.core.models;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.IListener;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterConditionChain;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.IFilterCondition;
import com.blotty.core.modifiers.filters.FilterConditionChain.FilterConditionConjunction;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterConditionChain;
import com.blotty.core.types.IGenericField;

public class DataModelView extends AbstractDataModel implements IListener<RowEvent> {

	private final FilterExpression filter;
	private final DataModel parentDataModel;
	private SorterConditionChain sorter;

	public DataModelView(String id, FilterExpression filter, DataModel parentDataModel) throws RowsModelException, FilterExpressionException {
		this(id, filter, null, parentDataModel);
	}

	public DataModelView(String id, SorterConditionChain sorter, DataModel parentDataModel) throws RowsModelException, FilterExpressionException {
		this(id, null, sorter, parentDataModel);
	}

	public DataModelView(String id, FilterExpression filter, SorterConditionChain sorter, DataModel parentDataModel) throws RowsModelException, FilterExpressionException {
		super(id, new InMemoryRowsModel(parentDataModel.getColumnsModel()));
		this.filter = filter;
		this.sorter = sorter;
		this.parentDataModel = parentDataModel;
		populate();

		if ( sorter!=null ) {
			sort( sorter );
		}

		this.parentDataModel.addRowsListener(id, this);
	}

	private void populate() throws RowsModelException, FilterExpressionException {
		// No filtering
		if ( filter==null ) {
			// TODO: add a clone method for faster replication
			parentDataModel.stream( new IConsumer<Row>() {
				@Override
				public boolean stream(Row row) throws RowsModelException, FilterExpressionException {
					addRow( row );
					return true;
				}			
			});	
		}
	
		// If there's an index...use it
		if (parentDataModel.hasIndexes()) {	
			FilterConditionChain current = filter.getConditionsChain();
		    while ( current.getNextCondition() != null ) {
		        FilterConditionConjunction conj = current.getConjunction();
		         
		        current = current.getNextCondition();
		        IFilterCondition cond = current.getCondition();
		        
		    	if ( cond instanceof BinaryCondition ) {
					Operand lCond = ((BinaryCondition)cond).getLeft();
					Operand rCond = ((BinaryCondition)cond).getRight();
					Column  searchCol = null;

					IGenericField searchVal = null;
					if (lCond.isConst()) {
						searchVal = lCond.getConst();
						searchCol = (!rCond.isConst())?rCond.getColumn():null;
					} else if (rCond.isConst()) {
						searchVal = rCond.getConst();
						searchCol = (!lCond.isConst())?lCond.getColumn():null;
					}

					if (searchVal!=null && searchCol!=null) {
						SearchIndex idx = searchCol.getSearchIndex();

						Column col = idx.getColumn();
						if (col.getId()==searchCol.getId()) {
							idx.stream(searchVal, new IConsumer<Row>() {
								@Override
								public boolean stream(Row row) throws RowsModelException {
									addRow( row );
									return true;
								}			
							});
							return;
						} 
					}
				}
		    }
		}

		// Otherwise fullscan
		parentDataModel.stream( new IConsumer<Row>() {
			@Override
			public boolean stream(Row row) throws RowsModelException, FilterExpressionException {
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
		this.sorter = sorter;
		try {
			getRowsModel().sort(this.sorter);
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
