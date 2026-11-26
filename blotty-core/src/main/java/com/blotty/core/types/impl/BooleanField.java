/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types.impl;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class BooleanField implements IGenericField<Boolean> {

	private boolean value;
	
	public static final BooleanField TRUE = new BooleanField( true );
	public static final BooleanField FALSE = new BooleanField( false );
	
	public static BooleanField of(String v) {
	    if (v == null) return FALSE;
	    String s = v.trim();
	    return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") 
	            || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("ok") 
	            || s.equals("1")) ? TRUE : FALSE;
	}

	@Override
	public int compareTo(IGenericField<Boolean> other) {
	    if (other.isNull()) return 1;
	    return Boolean.compare(this.value, ((BooleanField)other).value);
	}
	
	@Override
	public int compareTo(Boolean o) {
		if (o==null) return 1;
		if (value==o.booleanValue()) return 0;
		return (value)?1:-1;
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

	public boolean getValue() {
		return value;
	}

	@Override
	public Boolean getDefaultMax() {
		return null;
	}

	@Override
	public Boolean getDefaultMin() {
		return null;
	}

	@Override
	public Boolean value() {
		return value;
	}
}
