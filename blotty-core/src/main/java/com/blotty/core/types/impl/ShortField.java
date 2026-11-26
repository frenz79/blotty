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

public class ShortField implements IGenericField<ShortField> {

	private short value;
	
	public static final ShortField ZERO = new ShortField( (short)0 );
	public static final ShortField ONE = new ShortField( (short)1 );
	public static final ShortField MAX_VALUE = new ShortField( Short.MAX_VALUE );
	public static final ShortField MIN_VALUE = new ShortField( Short.MIN_VALUE );
	public static final ShortField NULL = new ShortField( (short)0 ) {
		@Override
		public boolean isNull() {
			return true;
		}
	};
	
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
	public int compareTo(ShortField other) {
	    return Short.compare(this.value, ((ShortField)other).value);
	}
	
	public short getValue() {
		return value;
	}
}
