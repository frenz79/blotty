package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.models.Row;
import com.blotty.core.common.models.modifiers.filters.FilterConditionChain.FilterConditionChainOperator;

public class FilterExpression {
	
	private final FilterConditionChain conditionsChain;
	
	public FilterExpression( FilterCondition condition ) {
		this.conditionsChain = new FilterConditionChain( condition );
	}
	
	public FilterExpression( FilterCondition ...conditions ) {
		this.conditionsChain = new FilterConditionChain( conditions[0] );
			
		FilterConditionChain chainRef = this.conditionsChain;
		for ( int i=1; i<conditions.length; i++ ) {
			chainRef = addConditionToChain( chainRef.getNextCondition(), conditions[i] );
		}
	}
	
	public FilterExpression( FilterConditionChain chain ) {
		this.conditionsChain = chain;
	}
		
	private FilterConditionChain addConditionToChain(FilterConditionChain nextCondition, FilterCondition filterCondition) {
		nextCondition = new FilterConditionChain( filterCondition ); 
		return nextCondition;
	}

	public boolean apply( Row row ) {		
		// Just 1 condition
		if ( conditionsChain.getNextCondition()==null ) {
			FilterCondition condition = conditionsChain.getCondition();
			return condition.apply( 
				row.getFields()[ condition.getLeftOperand().getId()] 
			);
		}
		return applyChain( conditionsChain, row );
	}
	
	private boolean applyChain( FilterConditionChain chainRef, Row row ) {
		if (chainRef==null) {
			return true;
		}
		FilterCondition condition = chainRef.getCondition();
		
		boolean conditionResult = condition.apply( 
			row.getFields()[ condition.getLeftOperand().getId()] 
		);
		// This is false...
		if ( !conditionResult ) {
			if (FilterConditionChainOperator.AND.equals(chainRef.getOperator())) {
				return false;
			}
			// Evaluate next one
			return applyChain( chainRef.getNextCondition(), row );
		}
		// ..else true
		if (FilterConditionChainOperator.OR.equals(chainRef.getOperator())) {
			return true;
		}
		// Evaluate next one
		return applyChain( chainRef.getNextCondition(), row );
	}
}
