package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.impl.StringField;

public class Like extends BinaryCondition {

	public Like(Column leftOperand, GenericField rightOperand) {
		super(leftOperand, rightOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return ((StringField)getRightOperand()).like((StringField)f);
	}

}
