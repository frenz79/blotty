package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class ShortField implements GenericField {

	private short value;
	
	public static final ShortField ZERO = new ShortField( (short)0 );
	public static final ShortField ONE = new ShortField( (short)1 );
	
	public static ShortField of(String str) {
		return new ShortField( Short.valueOf(str) );
	}

	public static ShortField of(short v) {
		switch(v) {
		case 0: return ZERO;
		case 1: return ONE;
		}
		return new ShortField( v );
	}
	
	ShortField( short v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.SHORT_TYPE;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof ShortField
		    	&& value == ((ShortField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(GenericField other) {
		return value-((ShortField)other).value;
	}
}
