/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters;

import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public class FilterConditionChain {

	public static enum FilterConditionConjunction {
		AND,
		OR
	};
	
	private final IFilterCondition condition;
	private FilterConditionConjunction conjunction;
	private FilterConditionChain nextCondition;
	
	public FilterConditionChain( IFilterCondition condition, FilterConditionConjunction op, FilterConditionChain nextCondition) {
		this.condition = condition;
		this.conjunction = op;
		this.nextCondition = nextCondition;
	}
	
	public FilterConditionChain( IFilterCondition c) {
		this(c, FilterConditionConjunction.AND, null);
	}

	public FilterConditionChain getNextCondition() {
		return nextCondition;
	}

	public void setNextCondition(FilterConditionChain nextCondition) {
		this.nextCondition = nextCondition;
	}

	public IFilterCondition getCondition() {
		return condition;
	}

	@Override
	public String toString() {
		return "FilterConditionChain [condition=" + condition + ", conjunction=" + conjunction + ", nextCondition="
				+ nextCondition + "]";
	}

	public FilterConditionConjunction getConjunction() {
		return conjunction;
	}

	public void setConjunction(FilterConditionConjunction conjunction) {
		this.conjunction = conjunction;
	}
}
