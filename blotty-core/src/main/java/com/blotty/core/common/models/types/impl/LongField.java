package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class LongField implements GenericField {

	private long value;
	
	public static final LongField ZERO = new LongField( 0l );
	public static final LongField ONE = new LongField( 1l );
	public static final LongField TEN = new LongField( 10l );
	public static final LongField ONE_HUNDRED = new LongField( 100l );
	public static final LongField ONE_THOUSAND = new LongField( 1000l );
	
	public static LongField of(String str) {
		return new LongField( Long.valueOf(str) );
	}

	public static LongField of(long v) {
		switch((int)v) {
		case 0: return ZERO;
		case 1: return ONE;
		case 10: return TEN;
		case 100: return ONE_HUNDRED;
		case 1000: return ONE_THOUSAND;
		}
		return new LongField( v );
	}
	
	LongField( long v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.LONG_TYPE;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof LongField
		    	&& value == ((LongField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(GenericField other) {
		return (int)(value-((LongField)other).value);
	}
}

