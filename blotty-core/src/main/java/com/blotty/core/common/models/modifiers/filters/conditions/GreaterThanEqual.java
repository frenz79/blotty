package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public class GreaterThanEqual extends BinaryCondition {

	public GreaterThanEqual(Column leftOperand, GenericField rightOperand) {
		super(leftOperand, rightOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return f.compareTo(getRightOperand())>=0;
	}

}
