package com.blotty.core.query.v2.models;

public final class UnionNode extends BinaryPredicate {

	public UnionNode(QuerySource left, QuerySource right) {
		super(left, right);
	}
}
