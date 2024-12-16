package com.blotty.core.common.models.modifiers.filters.conditions.binary.operators;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.IBinaryOperator;

public class Equal extends IBinaryOperator {

	@Override
	public boolean apply(GenericField f1, GenericField f2) {
		return f1.equals(f2);
	}

}
