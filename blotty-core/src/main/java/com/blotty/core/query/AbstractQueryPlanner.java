package com.blotty.core.query;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.query.models.FullScanSource;

public abstract class AbstractQueryPlanner {
	
	protected abstract QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model)
			throws RowsModelException, FilterExpressionException;

	public QueryPlan plan( QueryDefinition queryDef, AbstractDataModel model) 
			throws RowsModelException, FilterExpressionException {
		
		if (queryDef == null || !queryDef.hasFilter()) {
			return new QueryPlan(new FullScanSource(null, model));
		}
		if (!model.hasIndexes()) {
			return new QueryPlan(new FullScanSource(queryDef.getFilter().getRoot(), model));
		}		
		return apply( queryDef, model );
	}
}