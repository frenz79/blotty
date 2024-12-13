package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.exceptions.FilterExpressionException;
import com.blotty.core.common.models.modifiers.filters.FilterConditionChain.FilterConditionChainOperator;

public class FilterExpressionBuilder {

	private FilterExpression expression;
	private FilterConditionChain chainRef;
	
	public FilterExpressionBuilder() {
	}
	
	public FilterExpressionBuilder begin( FilterCondition c) {			
		this.chainRef = new FilterConditionChain( c ); 
		this.expression = new FilterExpression( this.chainRef );
		return this;
	}
	
	public FilterExpressionBuilder or(FilterCondition c) throws FilterExpressionException {
		return concat( c, FilterConditionChainOperator.OR);
	}
	
	public FilterExpressionBuilder and(FilterCondition c) throws FilterExpressionException {
		return concat( c, FilterConditionChainOperator.AND);
	}
	
	private FilterExpressionBuilder concat( FilterCondition c, FilterConditionChainOperator op) throws FilterExpressionException {
		if (chainRef==null) {
			throw new FilterExpressionException("Cannot concatenate expression.Please call begin() first.");
		}
		chainRef.setOperator( op );
		chainRef.setNextCondition( new FilterConditionChain( c ) );
		chainRef = chainRef.getNextCondition();
		return this;
	}
	
	public FilterExpression build() {
		return expression;
	}
}
