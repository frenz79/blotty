package com.blotty.core.query;

import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public class QueryPlan {
	
	private final QuerySource source;
	private final IFilterCondition residualFilter;
	
	public QueryPlan(QuerySource source) {
		this(source,null);
	}

	public QueryPlan(QuerySource source, IFilterCondition residualFilter) {
		this.source = source;
		this.residualFilter = residualFilter;
	}
	
	public QuerySource getSource() {
		return source;
	}
	
	public int getEstimatedRows() {
		// TODO
		return -1;
	}
	
	public double getEstimatedComplexity() {
		// TODO
		return -1;
	}
	
	public Object exaplainPlan() {
		// TODO
		return null;
	}

	public IFilterCondition getResidualFilter() {
		return residualFilter;
	}
}
