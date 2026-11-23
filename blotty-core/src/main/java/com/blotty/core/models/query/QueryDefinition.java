package com.blotty.core.models.query;

import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.sorters.SorterConditionChain;

public class QueryDefinition {
	
	private FilterExpression filter;
	private SorterConditionChain sorter;
	
	public QueryDefinition(FilterExpression filter, SorterConditionChain sorter) {
		super();
		this.filter = filter;
		this.sorter = sorter;
	}
	
	public QueryDefinition() {
		super();
	}
	
	public FilterExpression getFilter() {
		return filter;
	}
	
	public void setFilter(FilterExpression filter) {
		this.filter = filter;
	}
	
	public SorterConditionChain getSorter() {
		return sorter;
	}
	
	public void setSorter(SorterConditionChain sorter) {
		this.sorter = sorter;
	}
	
	public boolean hasSorter() {
		return sorter!=null;
	}
	
	public boolean hasFilter() {
		return filter!=null;
	}
	
}
