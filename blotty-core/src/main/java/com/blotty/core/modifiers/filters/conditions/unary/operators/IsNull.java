package com.blotty.core.modifiers.filters.conditions.unary.operators;

import com.blotty.core.modifiers.filters.conditions.unary.IUnaryOperator;
import com.blotty.core.types.IGenericField;

public class IsNull extends IUnaryOperator {

	@Override
	public boolean apply(IGenericField f) {
		return f.isNull();
	}

}
