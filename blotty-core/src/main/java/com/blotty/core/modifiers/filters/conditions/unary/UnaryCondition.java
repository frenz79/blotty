package com.blotty.core.modifiers.filters.conditions.unary;

import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.types.IGenericField;

public class UnaryCondition implements IFilterCondition {
		
	private final Operand left;
	private final IUnaryOperator operator;
	
	public UnaryCondition(Operand leftOperand, IUnaryOperator operator) {
		this.operator = operator;
		this.left = leftOperand;
	}

	@Override
	public boolean apply(Row row) {
		IGenericField[] fields = row.getFields();
		return operator.apply(fields[left.getColumn().getId()]);
	}
}
