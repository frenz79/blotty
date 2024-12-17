package com.blotty.core.modifiers.filters.conditions;

import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.types.IGenericField;
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
	
	public boolean isConst() {
		return type.equals(Type.CONST);
	}

	public boolean isColumn() {
		return type.equals(Type.COLUMN);
	}
	
	@Override
	public String toString() {
		return "Operand [type=" + type + ", operand=" + operand + "]";
	}
}
