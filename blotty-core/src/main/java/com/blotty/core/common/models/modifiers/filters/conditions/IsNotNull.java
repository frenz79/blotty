package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public class IsNotNull extends UnaryCondition {

	public IsNotNull(Column leftOperand) {
		super(leftOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return !f.isNull();
	}
}
