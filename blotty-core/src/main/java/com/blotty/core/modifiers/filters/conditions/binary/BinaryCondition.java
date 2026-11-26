/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions.binary;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotLike;
import com.blotty.core.rows.Row;
import com.blotty.core.types.IGenericField;

public class BinaryCondition implements IFilterCondition {
	
	private final Operand left;
	private final Operand right;
	private final IBinaryOperator operator;
	/*
	public BinaryCondition(Operand leftOperand, IBinaryOperator operator, Operand rightOperand ) {
		this.left = leftOperand;
		this.right = rightOperand;
		this.operator = operator;
	}
	*/
	
	// Reuse cached strings for fast comparison ==
	public BinaryCondition(Operand leftOperand, IBinaryOperator operator, Operand rightOperand) {
		this.operator = operator;
		this.left = resolveConst(rightOperand, leftOperand);
		this.right = resolveConst(leftOperand, rightOperand);
	}

	private static Operand resolveConst(Operand colOperand, Operand constOperand) {
		if (colOperand.isColumn() && colOperand.getColumn().isCacheable() && constOperand.isConst()) {
			IGenericField<?> current = constOperand.getConst();
			IGenericField<?> resolved = colOperand.getColumn().resolveCachedField(current);
			return (resolved == current) ? constOperand : new Operand(resolved, Operand.Type.CONST);
		}
		return constOperand;
	}
		
	public Operand getRight() {
		return right;
	}

	public Operand getLeft() {
		return left;
	}
	
	@Override
	public boolean apply( Row row ) throws FilterExpressionException {
	    IGenericField<?>[] fields = row.getFields();
	    if (left.isConst() && right.isConst()) {
	    	throw new FilterExpressionException("Both operand cannot be constants");
	    }
	    IGenericField<?> leftField = left.isConst() ? left.getConst() : fields[left.getColumn().getId()];
	    IGenericField<?> rightField = right.isConst() ? right.getConst() : fields[right.getColumn().getId()];
	    return operator.apply( leftField, rightField );
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
