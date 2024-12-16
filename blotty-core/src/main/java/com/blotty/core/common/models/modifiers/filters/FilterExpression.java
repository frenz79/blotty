package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.models.Row;
import com.blotty.core.common.models.modifiers.filters.FilterConditionChain.FilterConditionConjunction;

public class FilterExpression {
	
	private final FilterConditionChain conditionsChain;
	
	public FilterExpression( IFilterCondition condition ) {
		this.conditionsChain = new FilterConditionChain( condition );
	}
	
	public FilterExpression( IFilterCondition ...conditions ) {
		this.conditionsChain = new FilterConditionChain( conditions[0] );
			
		FilterConditionChain chainRef = this.conditionsChain;
		for ( int i=1; i<conditions.length; i++ ) {
			chainRef = addConditionToChain( chainRef.getNextCondition(), conditions[i] );
		}
	}
	
	public FilterExpression( FilterConditionChain chain ) {
		this.conditionsChain = chain;
	}
		
	private FilterConditionChain addConditionToChain(FilterConditionChain nextCondition, IFilterCondition filterCondition) {
		nextCondition = new FilterConditionChain( filterCondition ); 
		return nextCondition;
	}

	public boolean apply( Row row ) {		
		// Just 1 condition
		if ( conditionsChain.getNextCondition()==null ) {
			IFilterCondition condition = conditionsChain.getCondition();
			return condition.apply( row );
		}
		return applyChain( conditionsChain, row );
	}
	
	private boolean applyChain( FilterConditionChain chainRef, Row row ) {
		if (chainRef==null) {
			return true;
		}
		
		boolean conditionResult = chainRef.getCondition().apply( row ); 
		
		// This is false...
		if ( !conditionResult ) {
			if (FilterConditionConjunction.AND.equals(chainRef.getConjunction())) {
				return false;
			}
			// Evaluate next one
			return applyChain( chainRef.getNextCondition(), row );
		}
		// ..else true
		if (FilterConditionConjunction.OR.equals(chainRef.getConjunction())) {
			return true;
		}
		// Evaluate next one
		return applyChain( chainRef.getNextCondition(), row );
	}

	@Override
	public String toString() {
		return "FilterExpression [conditionsChain=" + conditionsChain + "]";
	}

	public FilterConditionChain getConditionsChain() {
		return conditionsChain;
	}
} 
