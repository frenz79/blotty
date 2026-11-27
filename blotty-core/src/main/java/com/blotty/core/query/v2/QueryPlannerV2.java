package com.blotty.core.query.v2;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.query.AbstractQueryPlanner;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.query.QueryPlan;
import com.blotty.core.query.v2.models.IntersectionNode;
import com.blotty.core.query.v2.models.PredicateNode;
import com.blotty.core.query.v2.models.QuerySource;
import com.blotty.core.query.v2.models.UnionNode;

public class QueryPlannerV2 extends AbstractQueryPlanner {

	@Override
	public QueryPlan apply(QueryDefinition queryDef, AbstractDataModel model) throws RowsModelException, FilterExpressionException {
	
		PredicateNode root;
		visit( queryDef.getFilter().getRoot(), null );
		
		return null;
	}

	private IFilterCondition visit(IFilterCondition parentCondition, PredicateNode parentNode) {
		for ( IFilterCondition condition : parentCondition.children() ) {
			if (condition instanceof AndCondition and) {
				IntersectionNode node = new IntersectionNode(
					new QuerySource( visit( and.getLeft() , parentNode)  ),
					new QuerySource( visit( and.getRight(), parentNode) )
				);			
			} else if (condition instanceof OrCondition or) {
				UnionNode node = new UnionNode(
					new QuerySource( visit( or.getLeft() , parentNode)  ),
					new QuerySource( visit( or.getRight(), parentNode) )
				);			
			}
		}
		return null;
	}

}
