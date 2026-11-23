package com.blotty.core.types.impl;

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
		return new String(value);
	}

	@Override
	public int compareTo(IGenericField other) {
		if (other.isNull()) return 1;
		return Arrays.compare(value, ((StringField)other).value);
	}

	// TODO: Optimize me!!!
	public boolean contains(StringField other) {
	    if (other == null) return false;
	    return this.toString().contains(other.toString());
	}	
}
