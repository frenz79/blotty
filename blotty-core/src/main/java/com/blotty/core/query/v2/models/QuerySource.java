package com.blotty.core.query.v2.models;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.rows.Row;

public abstract class QuerySource {
	
	private final int estimatedCardinality;
	
	public QuerySource(int estimatedCardinality) {
		this.estimatedCardinality = estimatedCardinality;
	}
	
	public QuerySource() {
		this(Integer.MAX_VALUE);
	}
	
	public int estimatedCardinality() {
		return estimatedCardinality;
	}
	
	public abstract void apply( IConsumer<Row> consumer ) throws RowsModelException, FilterExpressionException;
}
