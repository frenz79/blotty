package com.blotty.core.query.v2.models;

public abstract class BinaryPredicate extends PredicateNode  {

	private final QuerySource left;
	private final QuerySource right;
	
	public BinaryPredicate(QuerySource left, QuerySource right) {
		super();
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
