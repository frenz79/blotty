package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class DoubleField implements GenericField {

	private double value;
	
	public static final DoubleField ZERO = new DoubleField( 0.0d );
	public static final DoubleField ONE = new DoubleField( 1.0d );
	
	public static DoubleField of(String str) {
		return new DoubleField( Double.valueOf(str) );
	}

	public static DoubleField of(double v) {
		if ( v==0.0 ) {
			return ZERO;
		}
		if ( v==1.0 ) {
			return ONE;
		}
		return new DoubleField( v );
	}
	
	DoubleField( double v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.DOUBLE_TYPE;
	}
	
	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof DoubleField
		    	&& value == ((DoubleField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(GenericField other) {
		return (int)(value-((DoubleField)other).value);
	}
}

