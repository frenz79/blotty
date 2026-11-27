package com.blotty.core.query.v2.models;

import com.blotty.core.AbstractDataModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.rows.Row;

public class FullScanSource extends QuerySource {

	private final IFilterCondition condition;
	private final AbstractDataModel dataModel;
	
	public FullScanSource( IFilterCondition condition, AbstractDataModel dataModel) {
		super(dataModel.getRowsCount());
		this.condition = condition;
		this.dataModel = dataModel;
	}

	public IFilterCondition getCondition() {
		return condition;
	}

	@Override
	public void apply(IConsumer<Row> consumer) throws RowsModelException, FilterExpressionException {
		dataModel.stream(
			row -> {
				if (condition.apply(row)) {
					consumer.stream(row);
				}
				return true;
			}				
		);
	}

	public AbstractDataModel getDataModel() {
		return dataModel;
	}
}
