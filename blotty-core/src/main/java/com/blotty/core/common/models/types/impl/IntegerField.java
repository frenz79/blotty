package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class IntegerField implements GenericField {

	private int value;
	
	public static IntegerField of(String str) {
		return new IntegerField( Integer.valueOf(str) );
	}

	public static IntegerField of(int v) {
		return new IntegerField( v );
	}
	
	IntegerField( int v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.INTEGER_TYPE;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof IntegerField
		    	&& value == ((IntegerField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(GenericField other) {
		return value-((IntegerField)other).value;
	}
}
