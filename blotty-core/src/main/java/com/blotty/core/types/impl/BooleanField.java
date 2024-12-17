package com.blotty.core.types.impl;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class BooleanField implements IGenericField {

	private boolean value;
	
	public static final BooleanField TRUE = new BooleanField( true );
	public static final BooleanField FALSE = new BooleanField( false );
	
	public static BooleanField of(String v) {
		return (v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("y") || v.equalsIgnoreCase("ok"))?TRUE:FALSE;
	}
	
	public static BooleanField of(int b) {
		return (b!=0)?TRUE:FALSE;
	}

	public static BooleanField of(boolean v) {
		return (v)?TRUE:FALSE;
	}
	
	BooleanField( boolean v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.BOOLEAN_TYPE;
	}
	
	@Override
	public int hashCode() {
		return (value)?1:0;
	}
	
	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof BooleanField
		    	&& value == ((BooleanField)other).value)
		;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int compareTo(IGenericField other) {
		return (value==((BooleanField)other).value)?0:1;
	}
}
