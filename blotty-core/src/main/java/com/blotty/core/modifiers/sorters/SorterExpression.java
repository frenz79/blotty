/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.sorters;

import com.blotty.core.columns.Column;
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
