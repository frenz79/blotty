package com.blotty.core.common.models.modifiers.filters.conditions.binary;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.conditions.IOperator;

public abstract class IBinaryOperator implements IOperator {

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public abstract boolean apply(GenericField f1, GenericField f2);

}
