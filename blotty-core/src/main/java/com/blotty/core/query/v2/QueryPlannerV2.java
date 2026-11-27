package com.blotty.core.query.v2;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.query.AbstractQueryPlanner;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.query.v2.QueryPlan;
import com.blotty.core.query.v2.models.ConditionSource;
import com.blotty.core.query.v2.models.IntersectionSource;
import com.blotty.core.query.v2.models.QuerySource;
import com.blotty.core.query.v2.models.UnionSource;

public class QueryPlannerV2 extends AbstractQueryPlanner {

	@Override
	public QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model) throws RowsModelException, FilterExpressionException {
		QuerySource root = buildPlan( queryDef.getFilter().getRoot() );
		return new QueryPlan(root);
	}

	private QuerySource buildPlan(IFilterCondition condition) {
		if (condition instanceof AndCondition and) {
			return new IntersectionSource(
				buildPlan( and.getLeft() ),
				buildPlan( and.getRight()) );			
		}

		if (condition instanceof OrCondition or) {
			return new UnionSource(
				buildPlan( or.getLeft() ),
				buildPlan( or.getRight()) );			
		}
		
		return new ConditionSource(condition);
	}
}
