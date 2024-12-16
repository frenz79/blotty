package com.blotty.core.common.models.modifiers.filters.conditions.unary;

import com.blotty.core.common.models.Row;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.IFilterCondition;
import com.blotty.core.common.models.modifiers.filters.conditions.Operand;

public class UnaryCondition implements IFilterCondition {
		
	private final Operand left;
	private final IUnaryOperator operator;
	
	public UnaryCondition(Operand leftOperand, IUnaryOperator operator) {
		this.operator = operator;
		this.left = leftOperand;
	}

	@Override
	public boolean apply(Row row) {
		GenericField[] fields = row.getFields();
		return operator.apply(fields[left.getColumn().getId()]);
	}
}
