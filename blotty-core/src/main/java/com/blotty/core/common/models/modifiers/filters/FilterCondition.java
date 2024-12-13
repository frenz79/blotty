package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public abstract class FilterCondition {

	private final Column leftOperand;

	public FilterCondition(Column leftOperandCol) {
		this.leftOperand = leftOperandCol;
	}

	public Column getLeftOperand() {
		return leftOperand;
	}
	
	public abstract boolean apply( GenericField f );
}
