/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.query;

import java.util.List;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.indexes.AbstractIndex;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.query.QueryPlan.RowSet;
import com.blotty.core.rows.Row;

public class QueryPlanner {

	 /**
     * Creates a query execution plan.
     *
     * Strategy:
     *
     * 1) If no filter is defined, the whole model becomes
     *    the candidate dataset.
     *
     * 2) Extract all leaf conditions from the filter AST.
     *
     * 3) Ask every available index if it can support one of
     *    those conditions.
     *
     * 4) Choose the most selective index
     *    (smallest estimated cardinality).
     *
     * 5) Build a residual filter removing the condition already
     *    satisfied by the chosen index.
     *
     * Example:
     *
     *      STATUS='OPEN'
     *      AND
     *      QTY > 100
     *
     * If a HashIndex(STATUS) is available:
     *
     *      candidateRows = index.lookup(STATUS='OPEN')
     *      residualFilter = QTY > 100
     *
     * The residual filter is evaluated later by the view.
     *
     * NOTE:
     * OR branches are not optimized in the current implementation.
     * NOT subtrees are intentionally ignored by the ConditionCollector.
     */
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

			 /**
             * Evaluate all indexable conditions and
             * keep the most selective index access path.
             */
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
			 /**
             * If a suitable index has been found,
             * use it as candidate generator and
             * compute the residual filter.
             */
			if (bestIndex != null) {
				IFilterCondition residual = removeCondition(	queryDef.getFilter().getRoot(), bestCond );
				return new QueryPlan(
					bestIndex.lookup(bestCond),
					residual
				);
			}			
		}
		/**
         * Fallback:
         * full scan over all rows.
         */
		return useAllRows(model, queryDef.getFilter().getRoot());
	}

	/**
     * Removes a condition from an AND subtree.
     *
     * Example:
     *
     *      A AND B AND C
     *
     * removing B becomes:
     *
     *      A AND C
     *
     * This is safe because the candidate set
     * produced by the index already guarantees
     * that B is true.
     *
     * IMPORTANT:
     *
     * This optimization is intentionally restricted
     * to AND trees.
     *
     * For OR trees:
     *
     *      A OR B
     *
     * removing A would produce wrong results,
     * therefore OR branches are preserved.
     */
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
