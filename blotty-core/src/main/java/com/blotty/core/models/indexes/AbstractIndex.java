package com.blotty.core.models.indexes;

import java.util.Collection;

import com.blotty.core.models.Column;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public abstract class AbstractIndex {

	private final Column column;

	public abstract boolean supports( IFilterCondition c );
	public abstract Collection<Row> lookup( IFilterCondition c );
	
	public long estimatedCardinality(IFilterCondition c) {
		return lookup(c).size();
	}
	
	public AbstractIndex(Column column) {
		super();
		this.column = column;
	}

	public Column getColumn() {
		return column;
	}
	
}
