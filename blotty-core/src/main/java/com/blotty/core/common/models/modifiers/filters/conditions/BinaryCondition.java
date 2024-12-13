package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.FilterCondition;

public abstract class BinaryCondition extends FilterCondition {
	
	private final GenericField rightOperand;
	
	public BinaryCondition(Column leftOperand, GenericField rightOperand) {
		super(leftOperand);
		this.rightOperand = rightOperand;
	}

	public GenericField getRightOperand() {
		return rightOperand;
	}

}
