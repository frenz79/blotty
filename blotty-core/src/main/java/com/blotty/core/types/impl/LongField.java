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

public class LongField implements IGenericField {

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
	    if (v == 0L) return ZERO;
	    if (v == 1L) return ONE;
	    if (v == 10L) return TEN;
	    if (v == 100L) return ONE_HUNDRED;
	    if (v == 1000L) return ONE_THOUSAND;
	    return new LongField( v );
	}

	@Override
	public int compareTo(IGenericField other) {
	    if (other.isNull()) return 1;
	    return Long.compare(this.value, ((LongField)other).value);
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

	public long getValue() {
		return value;
	}
}

