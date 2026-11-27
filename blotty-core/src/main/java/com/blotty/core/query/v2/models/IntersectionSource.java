package com.blotty.core.query.v2.models;

import java.util.HashSet;
import java.util.Set;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.rows.Row;

public final class IntersectionSource extends BinaryQuerySource {

	public IntersectionSource(QuerySource left, QuerySource right) {
		super(left, right);
	}

	/**
	 * intersection 
	 * @throws FilterExpressionException 
	 * @throws RowsModelException 
	 **/
	@Override
	public void apply(IConsumer<Row> consumer) throws RowsModelException, FilterExpressionException {
		int lCardinality = getLeft().estimatedCardinality();
		int rCardinality = getRight().estimatedCardinality();
		
		if (lCardinality==0 || rCardinality==0) {
			// Nothing to do..one side is empty!
			return;
		}
		
		QuerySource first = getLeft();
		QuerySource second = getRight();
		int firstCardinality = lCardinality;
		
		if (rCardinality<lCardinality) {
			first = getRight();
			second = getLeft();
			firstCardinality = rCardinality;
		}
		
		Set<Row> firstRows = new HashSet<>(firstCardinality);
		
		first.apply(new IConsumer<>() {
			@Override
			public boolean stream(Row item) throws RowsModelException, FilterExpressionException {
				firstRows.add(item);
				return true;
			}
		});
		
		second.apply(new IConsumer<>() {
			@Override
			public boolean stream(Row item) throws RowsModelException, FilterExpressionException {
				if (firstRows.contains(item)) {
					consumer.stream(item);
				}
				return true;
			}
		});		
	}
}
