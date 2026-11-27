package com.blotty.core.query.v2.models;

import com.blotty.core.query.QuerySource;

public abstract class BinaryQuerySource extends QuerySource  {

	private final QuerySource left;
	private final QuerySource right;
	
	public BinaryQuerySource(QuerySource left, QuerySource right, int estimatedCardinality) {
		super(estimatedCardinality);
		this.left = left;
		this.right = right;
	}

	public QuerySource getLeft() {
		return left;
	}

	public QuerySource getRight() {
		return right;
	}
}
