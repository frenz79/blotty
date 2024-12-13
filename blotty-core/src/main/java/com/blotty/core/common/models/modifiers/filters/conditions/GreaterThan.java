package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public class GreaterThan extends BinaryCondition {

	public GreaterThan(Column leftOperand, GenericField rightOperand) {
		super(leftOperand, rightOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return f.compareTo(getRightOperand())>0;
	}

}
