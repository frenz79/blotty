package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class IntegerField implements GenericField {

	private int value;
	
	public static final IntegerField ZERO = new IntegerField( 0 );
	public static final IntegerField ONE = new IntegerField( 1 );
	public static final IntegerField TEN = new IntegerField( 10 );
	public static final IntegerField ONE_HUNDRED = new IntegerField( 100 );
	public static final IntegerField ONE_THOUSAND = new IntegerField( 1000 );
	
	public static IntegerField of(String str) {
		return new IntegerField( Integer.valueOf(str) );
	}

	public static IntegerField of(int v) {
		switch(v) {
		case 0: return ZERO;
		case 1: return ONE;
		case 10: return TEN;
		case 100: return ONE_HUNDRED;
		case 1000: return ONE_THOUSAND;
		}
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
