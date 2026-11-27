package com.blotty.core.query.models;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.indexes.AbstractIndex;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.query.QuerySource;
import com.blotty.core.rows.Row;

public class IndexSource extends QuerySource {

	private final AbstractIndex<?> index;
	private final IFilterCondition condition;
	
	public IndexSource(AbstractIndex<?> index, IFilterCondition condition) {
		super(index.estimatedCardinality(condition));
		this.index = index;
		this.condition = condition;
	}

	public AbstractIndex<?> getIndex() {
		return index;
	}

	public IFilterCondition getCondition() {
		return condition;
	}

	@Override
	public void apply(IConsumer<Row> consumer) throws RowsModelException, FilterExpressionException {
		for ( Row r : index.lookup(condition)) {
			if (!consumer.stream(r)) {
				break;
			}
		}
	}
}
