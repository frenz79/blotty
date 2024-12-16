package com.blotty.core.common.models.modifiers.filters.conditions.binary.operators;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.common.models.types.impl.StringField;

public class Like extends IBinaryOperator {

	@Override
	public boolean apply(GenericField f1, GenericField f2) {
		return ((StringField)f2).contains((StringField)f1);
	}

}
