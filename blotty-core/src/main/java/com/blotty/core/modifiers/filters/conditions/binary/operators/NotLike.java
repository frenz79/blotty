package com.blotty.core.modifiers.filters.conditions.binary.operators;

import com.blotty.core.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.StringField;

public class NotLike extends IBinaryOperator {

	@Override
	public boolean apply(IGenericField f1, IGenericField f2) {
		return !((StringField)f2).contains((StringField)f1);
	}

}
