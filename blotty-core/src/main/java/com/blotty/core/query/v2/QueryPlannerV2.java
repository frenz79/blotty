package com.blotty.core.query.v2;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.indexes.AbstractIndex;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.query.AbstractQueryPlanner;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.query.QueryPlan;
import com.blotty.core.query.QuerySource;
import com.blotty.core.query.models.FullScanSource;
import com.blotty.core.query.models.IndexSource;
import com.blotty.core.query.v2.models.IntersectionSource;
import com.blotty.core.query.v2.models.UnionSource;

public class QueryPlannerV2 extends AbstractQueryPlanner {

	@Override
	public QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model) throws RowsModelException, FilterExpressionException {
		QuerySource root = buildPlan( queryDef.getFilter().getRoot(), model);
		return new QueryPlan(root);
	}

	private QuerySource buildPlan(IFilterCondition condition, AbstractDataModel model) {
		if (condition instanceof AndCondition and) {
			return new IntersectionSource(
				buildPlan( and.getLeft(), model ),
				buildPlan( and.getRight(), model ));			
		}
		if (condition instanceof OrCondition or) {
			return new UnionSource(
				buildPlan( or.getLeft(), model ),
				buildPlan( or.getRight(), model ));			
		}
		if (condition instanceof NotCondition) {
			return new FullScanSource(condition, model);		
		}
		return buildLeaf(condition, model);
	}
	
	private QuerySource buildLeaf(IFilterCondition condition, AbstractDataModel model) {
		AbstractIndex<?> bestIndex = null;
		int bestCardinality = Integer.MAX_VALUE;
		
		for ( AbstractIndex<?> index : model.getIndexes() ) {
			if (!index.supports(condition) ) {
				continue;
			}
			int estimated = index.estimatedCardinality(condition);
			if (estimated<bestCardinality) {
				bestCardinality = estimated;
				bestIndex = index;
			}
		}
		
		if (bestIndex!=null) {
			return new IndexSource(bestIndex, condition);
		}
		return new FullScanSource(condition, model);
	}
}
