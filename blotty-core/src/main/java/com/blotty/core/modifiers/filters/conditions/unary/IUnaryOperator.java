package com.blotty.core.modifiers.filters.conditions.unary;

import com.blotty.core.modifiers.filters.conditions.IOperator;
import com.blotty.core.types.IGenericField;

public abstract class IUnaryOperator implements IOperator{

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public abstract boolean apply(IGenericField f);

}
