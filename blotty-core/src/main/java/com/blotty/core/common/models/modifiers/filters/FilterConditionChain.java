package com.blotty.core.common.models.modifiers.filters;

public class FilterConditionChain {

	public static enum FilterConditionChainOperator {
		AND,
		OR
	};
	
	private final FilterCondition condition;
	private FilterConditionChainOperator operator;
	private FilterConditionChain nextCondition;
	
	public FilterConditionChain( FilterCondition condition, FilterConditionChainOperator op, FilterConditionChain nextCondition) {
		this.condition = condition;
		this.operator = op;
		this.nextCondition = nextCondition;
	}
	
	public FilterConditionChain( FilterCondition condition) {
		this(condition, FilterConditionChainOperator.AND, null);
	}

	public FilterConditionChain getNextCondition() {
		return nextCondition;
	}

	public void setNextCondition(FilterConditionChain nextCondition) {
		this.nextCondition = nextCondition;
	}

	public FilterConditionChainOperator getOperator() {
		return operator;
	}

	public void setOperator(FilterConditionChainOperator operator) {
		this.operator = operator;
	}

	public FilterCondition getCondition() {
		return condition;
	}
}
