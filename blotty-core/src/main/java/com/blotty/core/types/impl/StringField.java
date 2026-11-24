/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class StringField implements IGenericField {

	private byte[] value;
	private int hash;
	
	public static StringField of( String v ) {
		return new StringField( v.getBytes(StandardCharsets.UTF_8) );
	}
	
	StringField( byte[] v ){
		this.value = v;
		this.hash = Arrays.hashCode( v );
	}
	
	@Override
	public FieldType getType() {
		return FieldType.STRING_TYPE;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals( Object other ) {
		return 
			this==other
			|| ( other instanceof StringField
		    	&& Arrays.equals(value, ((StringField)other).value)
			)
		;
	}

	@Override
	public String toString() {
		return new String(value, StandardCharsets.UTF_8);
	}

	@Override
	public int compareTo(IGenericField other) {
		if (other.isNull()) return 1;
		return Arrays.compare(value, ((StringField)other).value);
	}

	public boolean contains(StringField other) {
	    if (other == null) {
	        return false;
	    }
	
	    byte[] haystack = value;
	    byte[] needle = other.value;
	
	    int n = needle.length;
	    int h = haystack.length;
	
	    if (n == 0) {
	        return true;
	    }
	    if (n > h) {
	        return false;
	    }
	
	    byte first = needle[0];
	
	    outer:
	    for (int i = 0; i <= h - n; i++) {
	       if (haystack[i] != first) {
	            continue;
	        }	
	        for (int j = 1; j < n; j++) {
	            if (haystack[i + j] != needle[j]) {
	                continue outer;
	            }
	        }	
	        return true;
	    }	
	    return false;
	}
	
}
