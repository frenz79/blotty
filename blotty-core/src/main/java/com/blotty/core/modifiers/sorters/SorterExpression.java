package com.blotty.core.modifiers.sorters;

import com.blotty.core.models.Column;
import com.blotty.core.modifiers.sorters.operators.ISorterOperator;

public class SorterExpression {

	private final Column columm;
	private final ISorterOperator operator;
	
	public SorterExpression(Column columm, ISorterOperator operator) {
		super();
		this.columm = columm;
		this.operator = operator;
	}

	public Column getColumm() {
		return columm;
	}

	public ISorterOperator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return "SorterExpression [columm=" + columm + ", operator=" + operator + "]";
	}
}
