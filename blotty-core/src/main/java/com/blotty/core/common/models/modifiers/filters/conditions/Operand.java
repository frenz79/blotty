package com.blotty.core.common.models.modifiers.filters.conditions;

import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.Column;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.impl.IntegerField;
import com.blotty.core.common.models.types.impl.StringField;

public class Operand {

	static enum Type {
		CONST,
		FIELD
	}
	
	public static Operand of(String v, Column c) throws RowsTypeException {
		return new Operand(c.fieldOf(v), Type.CONST);
	}
	
	public static Operand of( Column c ) {
		return new Operand(c, Type.FIELD);
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
	
	public GenericField getConst() {
		return (GenericField)operand;
	}
	
	public boolean isConst() {
		return type.equals(Type.CONST);
	}

	@Override
	public String toString() {
		return "Operand [type=" + type + ", operand=" + operand + "]";
	}
}
