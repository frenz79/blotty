package com.blotty.core.common.models.modifiers.filters.conditions.binary;

import com.blotty.core.common.models.Row;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.modifiers.filters.IFilterCondition;
import com.blotty.core.common.models.modifiers.filters.conditions.Operand;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.LesserThanEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.NotLike;

public class BinaryCondition implements IFilterCondition {
	
	private final Operand left;
	private final Operand right;
	private final IBinaryOperator operator;
	
	public BinaryCondition(Operand leftOperand, IBinaryOperator operator, Operand rightOperand ) {
		this.left = leftOperand;
		this.right = rightOperand;
		this.operator = operator;
	}

	public Operand getRight() {
		return right;
	}

	public Operand getLeft() {
		return left;
	}
	
	@Override
	public boolean apply( Row row ) {
		GenericField[] fields = row.getFields();
		if ( right.isConst() ) {
			return operator.apply( fields[left.getColumn().getId()], right.getConst() );
		}
		return operator.apply( left.getConst(), fields[right.getColumn().getId()] );
	}

	public IBinaryOperator getOperator() {
		return operator;
	}
	
	public static BinaryCondition Equal( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new Equal(), op2);		
	}
	
	public static BinaryCondition NotEqual( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new NotEqual(), op2);		
	}

	public static BinaryCondition GreaterThan( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new GreaterThan(), op2);		
	}
	
	public static BinaryCondition GreaterThanEqual( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new GreaterThanEqual(), op2);		
	}
	
	public static BinaryCondition LesserThan( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new LesserThan(), op2);		
	}
	
	public static BinaryCondition LesserThanEqual( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new LesserThanEqual(), op2);		
	}
	
	public static BinaryCondition Like( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new Like(), op2);		
	}

	public static BinaryCondition NotLike( Operand op1, Operand op2 ) {
		return new BinaryCondition(op1, new NotLike(), op2);		
	}
	
	@Override
	public String toString() {
		return "BinaryCondition [leftOperand=" + left + ", rightOperand=" + right + ", operator="
				+ operator + "]";
	}
}
