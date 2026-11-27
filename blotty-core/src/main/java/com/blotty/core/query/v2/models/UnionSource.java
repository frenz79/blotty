package com.blotty.core.query.v2.models;

import java.util.HashSet;
import java.util.Set;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.query.QuerySource;
import com.blotty.core.rows.Row;

public final class UnionSource extends BinaryQuerySource {

	public UnionSource(QuerySource left, QuerySource right) {
		super(left, right, left.estimatedCardinality() + right.estimatedCardinality());
	}

	@Override
	public void apply(IConsumer<Row> consumer) throws RowsModelException, FilterExpressionException {
		int lCardinality = getLeft().estimatedCardinality();
		int rCardinality = getRight().estimatedCardinality();
		
		// Edge cases
		if (lCardinality==0 && rCardinality==0) {
			// Nothing to do..all sides are empty!
			return;
		}
		if (lCardinality==0 || rCardinality==0) {
			IConsumer<Row> tmpConsumer = new IConsumer<Row>() {
				@Override
				public boolean stream(Row item) throws RowsModelException, FilterExpressionException {
					consumer.stream(item);
					return true;
				}
			};
			
			if (lCardinality==0) {
				getRight().apply( tmpConsumer );
				return;
			}
			getLeft().apply( tmpConsumer );
			return;
		}
		
		Set<Row> unionSet = new HashSet<>(lCardinality+rCardinality);
		IConsumer<Row> tmpConsumer = new IConsumer<Row>() {
			@Override
			public boolean stream(Row item) throws RowsModelException, FilterExpressionException {
				unionSet.add(item);
				return true;
			}
		};
		getLeft().apply( tmpConsumer );
		getRight().apply( tmpConsumer );
		
		for ( Row r : unionSet) {
			consumer.stream(r);
		}
	}
}
