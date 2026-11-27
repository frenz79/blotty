package com.blotty.core.query.v2.models;

import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public class QuerySource {

	private final IFilterCondition condition;

	public QuerySource(IFilterCondition condition) {
		super();
		this.condition = condition;
	}

	public IFilterCondition getCondition() {
		return condition;
	}
	
}
