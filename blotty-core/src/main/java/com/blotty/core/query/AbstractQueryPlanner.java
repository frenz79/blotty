package com.blotty.core.query;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.query.QueryPlan.RowSet;
import com.blotty.core.rows.Row;

public abstract class AbstractQueryPlanner {
	
	protected abstract QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model)
			throws RowsModelException, FilterExpressionException;

	public QueryPlan plan( QueryDefinition queryDef, AbstractDataModel model)
		throws RowsModelException, FilterExpressionException {
		if (!queryDef.hasFilter() || !model.hasIndexes()) {
			return useAllRows(model, null);
		}
		QueryPlan plan = apply( queryDef, model );
		return (plan==null)?
			useAllRows(model, null):plan;
	}
	
	/**
     * Generates a plan performing a full scan of the model.
     *
     * All rows become candidates and the complete filter
     * is evaluated later.
     */
	private QueryPlan useAllRows(AbstractDataModel model, IFilterCondition residual) throws RowsModelException, FilterExpressionException {
		QueryPlan ret = new QueryPlan(residual);
		RowSet rows = ret.getCandidateRows();

		// TODO: add a clone method for faster replication
		model.stream( new IConsumer<Row>() {
			@Override
			public boolean stream(Row row) throws RowsModelException, FilterExpressionException {
				rows.addRow( row );
				return true;
			}			
		});
		return ret;
	}
}