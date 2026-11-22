package com.blotty.core.modifiers.filters;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.FilterConditionChain.FilterConditionConjunction;

public class FilterExpression {
	
	private final FilterConditionChain conditionsChain;
	
	public FilterExpression( IFilterCondition condition ) {
		this.conditionsChain = new FilterConditionChain( condition );
	}
	
	public FilterExpression( IFilterCondition ...conditions ) {
	    if (conditions == null || conditions.length == 0) {
	        throw new IllegalArgumentException("Conditions cannot be null or empty");
	    }
	    this.conditionsChain = new FilterConditionChain( conditions[0] );
	    FilterConditionChain current = this.conditionsChain;
	    for ( int i = 1; i < conditions.length; i++ ) {
	        FilterConditionChain next = new FilterConditionChain( conditions[i] );
	        current.setNextCondition( next );
	        current = next;
	    }
	}

	public boolean apply( Row row ) throws FilterExpressionException {
	    if ( conditionsChain == null ) return true;
	    boolean result = conditionsChain.getCondition().apply( row );
	    FilterConditionChain current = conditionsChain;
	    while ( current.getNextCondition() != null ) {
	        FilterConditionConjunction conj = current.getConjunction();
	        current = current.getNextCondition();
	        // Short-circuit: se con AND il risultato è già false, o con OR è già true
	        if ( conj == FilterConditionConjunction.AND && !result ) {
	            continue; // Salta il calcolo di nextVal
	        }
	        if ( conj == FilterConditionConjunction.OR && result ) {
	            return true; // Risultato già definitivamente true
	        }
	        boolean nextVal = current.getCondition().apply( row );
	        result = (conj == FilterConditionConjunction.AND) ? (result && nextVal) : (result || nextVal);
	    }
	    return result;
	}
	
	public FilterExpression( FilterConditionChain chain ) {
		this.conditionsChain = chain;
	}
	
	@Override
	public String toString() {
		return "FilterExpression [conditionsChain=" + conditionsChain + "]";
	}

	public FilterConditionChain getConditionsChain() {
		return conditionsChain;
	}
} 
