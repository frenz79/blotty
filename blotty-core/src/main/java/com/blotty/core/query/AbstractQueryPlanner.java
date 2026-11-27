package com.blotty.core.query;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;

public abstract class AbstractQueryPlanner {
	
	protected abstract QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model)
			throws RowsModelException, FilterExpressionException;

	public QueryPlan plan( QueryDefinition queryDef, AbstractDataModel model) 
			throws RowsModelException, FilterExpressionException {
		
		if (!model.hasIndexes()) {
			return new QueryPlan(
				null, queryDef.getFilter().getRoot()
			);
		}		
		return apply( queryDef, model );
	}
}