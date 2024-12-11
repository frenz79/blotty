package com.blotty.core.common.models.types.impl;

import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.GenericField;

public class NullField implements GenericField {

	public static final NullField NULL = new NullField();
	
	public boolean isNull() {
		return true;
	}

	@Override
	public FieldType getType() {
		return null;
	}
}
