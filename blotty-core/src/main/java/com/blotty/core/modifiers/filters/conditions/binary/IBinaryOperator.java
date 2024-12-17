package com.blotty.core.modifiers.filters.conditions.binary;

import com.blotty.core.modifiers.filters.conditions.IOperator;
import com.blotty.core.types.IGenericField;

public abstract class IBinaryOperator implements IOperator {

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public abstract boolean apply(IGenericField f1, IGenericField f2);

}
