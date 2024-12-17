package com.blotty.core.modifiers.filters.conditions.binary.operators;

import com.blotty.core.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.types.IGenericField;

public class NotEqual extends IBinaryOperator {

	@Override
	public boolean apply(IGenericField f1, IGenericField f2) {
		return !f1.equals(f2);
	}

}