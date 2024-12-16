package com.blotty.core.common.models.modifiers.filters.conditions.unary;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.conditions.IOperator;

public abstract class IUnaryOperator implements IOperator{

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public abstract boolean apply(GenericField f);

}
