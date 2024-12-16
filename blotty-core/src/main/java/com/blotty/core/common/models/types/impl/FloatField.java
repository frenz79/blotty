package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class FloatField implements GenericField {

	private float value;
	
	public static final FloatField ZERO = new FloatField( 0.0f );
	public static final FloatField ONE = new FloatField( 1.0f );
	
	public static FloatField of(String str) {
		return new FloatField( Float.valueOf(str) );
	}

	public static FloatField of(float v) {
		if ( v==0.0f ) {
			return ZERO;
		}
		if ( v==1.0f ) {
			return ONE;
		}
		return new FloatField( v );
	}
	
	FloatField( float v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.FLOAT_TYPE;
	}
	
	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof FloatField
		    	&& value == ((FloatField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(GenericField other) {
		return (int)(value-((FloatField)other).value);
	}
}

