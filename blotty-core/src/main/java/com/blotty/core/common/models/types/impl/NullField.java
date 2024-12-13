package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;

public class NullField implements GenericField {

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
		return false;
	}
	
	@Override
	public int compareTo(GenericField other) {
		return -1;
	}
}
