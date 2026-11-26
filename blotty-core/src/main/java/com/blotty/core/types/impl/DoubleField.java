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

public class DoubleField implements IGenericField<Double> {

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
	public int compareTo(IGenericField<Double> other) {
	    if (other.isNull()) return 1;
	    return Double.compare(this.value, ((DoubleField)other).value);
	}
	
	@Override
	public int compareTo(Double o) {
		if (o==null) return 1;
		return (int)(value - o.doubleValue());
	}

	public double getValue() {
		return value;
	}
	
	@Override
	public Double getDefaultMax() {
		return Double.MAX_VALUE;
	}

	@Override
	public Double getDefaultMin() {
		return Double.MIN_VALUE;
	}

	@Override
	public Double value() {
		return value;
	}
}

