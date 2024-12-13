package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;

public class IsNull extends UnaryCondition {

	public IsNull(Column leftOperand) {
		super(leftOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return f.isNull();
	}

}
