package com.blotty.core.models.query;

import java.util.List;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.AbstractDataModel;
import com.blotty.core.models.indexes.AbstractIndex;
import com.blotty.core.models.query.QueryPlan.RowSet;
import com.blotty.core.models.rows.Row;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public class QueryPlanner {

	public QueryPlan plan( QueryDefinition queryDef, AbstractDataModel model ) throws RowsModelException, FilterExpressionException {
		if (!queryDef.hasFilter()) {
			return useAllRows(model, null);
		}
		
		if (model.hasIndexes()) {
			List<IFilterCondition> conditions = new ConditionCollector()
				.collect( queryDef.getFilter().getRoot() );
	
			AbstractIndex<?> bestIndex = null;
			IFilterCondition bestCond = null;
			long bestCardinality = Long.MAX_VALUE;
	
			for ( IFilterCondition cond : conditions ) {
				for (AbstractIndex<?> index : model.getIndexes() ) {
					if (!index.supports(cond)) {
						continue;
					}
					long cardinality = index.estimatedCardinality(cond);
					if (cardinality < bestCardinality) {
						bestCardinality = cardinality;
						bestCond = cond;
						bestIndex = index;
					}
				}
			}
			
			if (bestIndex != null) {
				IFilterCondition residual = removeCondition(	queryDef.getFilter().getRoot(), bestCond );
				return new QueryPlan(
					bestIndex.lookup(bestCond),
					residual
				);
			}			
		}
		
		return useAllRows(model, queryDef.getFilter().getRoot());
	}
	
	private IFilterCondition removeCondition(IFilterCondition root, IFilterCondition condition) {
		if (root==condition) {
			return null;
		}
		if (!(root instanceof AndCondition and)) {
			return root;
		}
		IFilterCondition l = removeCondition(and.getLeft(), condition);
		IFilterCondition r = removeCondition(and.getRight(), condition);
		if (l==null) {
			return r;
		}
		if (r==null) {
			return l;
		}
		return new AndCondition(l, r);
	}

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
