package com.blotty.core.common.models.commons;

import com.blotty.core.common.models.types.FieldType;

public interface GenericField {

	public default boolean isNull() {
		return false;
	}
	
	public FieldType getType();
}
