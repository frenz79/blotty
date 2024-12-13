package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.modifiers.filters.FilterCondition;

public abstract class UnaryCondition extends FilterCondition {
		
	public UnaryCondition(Column leftOperand) {
		super(leftOperand);
	}
}
