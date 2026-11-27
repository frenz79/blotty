package com.blotty.core.query.v2.models;

public final class IntersectionNode extends BinaryPredicate {

	public IntersectionNode(QuerySource left, QuerySource right) {
		super(left, right);
	}

}
