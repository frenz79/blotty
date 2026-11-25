/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions;

import com.blotty.core.columns.Column;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.BooleanField;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.StringField;

public class Operand {

	public static enum Type {
		CONST,
		COLUMN
	}
	
	public static Operand of(String v, Column c) throws RowsTypeException {
		return new Operand(c.fieldOf(v), Type.CONST);
	}
	
	public static Operand of( Column c ) {
		return new Operand(c, Type.COLUMN);
	}
	
	public static Operand of( String c ) {
		return new Operand(StringField.of(c), Type.CONST);
	}
	
	public static Operand of( int c ) {
		return new Operand(IntegerField.of(c), Type.CONST);
	}
	
	public static Operand of( boolean c ) {
		return new Operand(BooleanField.of(c), Type.CONST);
	}
	
	private final Type type;
	private final Object operand;

	public Operand(Object operand, Type t) {
		super();
		this.operand = operand;
		this.type = t;
	}

	public Type getType() {
		return type;
	}

	public Object getOperand() {
		return operand;
	}
	
	public Column getColumn() {
		return (Column)operand;
	}
	
	public IGenericField getConst() {
		return (IGenericField)operand;
	}
	
	public boolean isColumn() {
	    return type == Type.COLUMN;
	}
	
	public boolean isConst() {
	    return type == Type.CONST;
	}
	
	@Override
	public String toString() {
		return "Operand [type=" + type + ", operand=" + operand + "]";
	}
}
