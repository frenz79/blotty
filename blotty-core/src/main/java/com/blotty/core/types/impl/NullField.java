package com.blotty.core.types.impl;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class NullField implements IGenericField {

	public static final NullField NULL = new NullField();
	
	public boolean isNull() {
		return true;
	}

	@Override
	public FieldType getType() {
		return FieldType.NULL;
	}
	
	@Override
	public boolean equals( Object other ) {
	    return other instanceof NullField;
	}
	@Override
	public int hashCode() {
	    return 0;
	}
	
	@Override
	public int compareTo(IGenericField other) {
	    if (other == null || other.isNull()) return 0;
	    return -1;
	}	  

	@Override
	public String toString() {
		return "NULL";
	}
}
