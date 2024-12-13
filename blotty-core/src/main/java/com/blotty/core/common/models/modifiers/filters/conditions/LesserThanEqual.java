package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public class LesserThanEqual extends BinaryCondition {

	public LesserThanEqual(Column leftOperand, GenericField rightOperand) {
		super(leftOperand, rightOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return f.compareTo(getRightOperand())<=0;
	}

}
