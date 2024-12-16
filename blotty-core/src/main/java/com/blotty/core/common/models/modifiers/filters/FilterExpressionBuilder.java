package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.exceptions.FilterExpressionException;
import com.blotty.core.common.models.modifiers.filters.FilterConditionChain.FilterConditionConjunction;

public class FilterExpressionBuilder {

	private FilterExpression expression;
	private FilterConditionChain chainRef;
	
	public FilterExpressionBuilder() {
	}
	
	public FilterExpressionBuilder begin( IFilterCondition c) {			
		this.chainRef = new FilterConditionChain( c ); 
		this.expression = new FilterExpression( this.chainRef );
		return this;
	}
	
	public FilterExpressionBuilder or(IFilterCondition c) throws FilterExpressionException {
		return concat( c, FilterConditionConjunction.OR);
	}
	
	public FilterExpressionBuilder and(IFilterCondition c) throws FilterExpressionException {
		return concat( c, FilterConditionConjunction.AND);
	}
	
	private FilterExpressionBuilder concat( IFilterCondition c, FilterConditionConjunction op) throws FilterExpressionException {
		if (chainRef==null) {
			throw new FilterExpressionException("Cannot concatenate expression.Please call begin() first.");
		}
		chainRef.setConjunction( op );
		chainRef.setNextCondition( new FilterConditionChain( c ) );
		chainRef = chainRef.getNextCondition();
		return this;
	}
	
	public FilterExpression build() {
		return expression;
	}
}
