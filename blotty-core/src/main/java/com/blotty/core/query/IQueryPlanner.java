package com.blotty.core.query;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;

public interface IQueryPlanner {

	QueryPlan plan(
			QueryDefinition queryDef, 
			AbstractDataModel model)
		throws RowsModelException, FilterExpressionException;
}