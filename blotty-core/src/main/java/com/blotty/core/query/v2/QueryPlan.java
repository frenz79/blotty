package com.blotty.core.query.v2;

import com.blotty.core.query.v2.models.QuerySource;

public class QueryPlan {
	
	private final QuerySource source;
	
	public QueryPlan(QuerySource source) {
		super();
		this.source = source;
	}

	public QuerySource getSource() {
		return source;
	}
}
