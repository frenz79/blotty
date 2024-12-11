package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.GenericField;

public class StringField implements GenericField {

	private byte[] value;
	
	public static StringField of( String v ) {
		return new StringField( v.getBytes() );
	}
	
	StringField( byte[] v ){
		this.value = v;
	}
	
	@Override
	public FieldType getType() {
		return FieldType.STRING_TYPE;
	}
	
}
