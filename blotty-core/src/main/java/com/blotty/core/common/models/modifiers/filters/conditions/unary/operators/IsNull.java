package com.blotty.core.common.models.modifiers.filters.conditions.unary.operators;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.conditions.unary.IUnaryOperator;

public class IsNull extends IUnaryOperator {

	@Override
	public boolean apply(GenericField f) {
		return f.isNull();
	}

}
