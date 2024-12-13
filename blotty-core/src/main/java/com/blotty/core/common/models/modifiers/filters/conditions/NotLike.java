package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.impl.StringField;

public class NotLike extends BinaryCondition {

	public NotLike(Column leftOperand, GenericField rightOperand) {
		super(leftOperand, rightOperand);
	}

	@Override
	public boolean apply(GenericField f) {
		return !((StringField)f).like((StringField)getRightOperand());
	}

}
