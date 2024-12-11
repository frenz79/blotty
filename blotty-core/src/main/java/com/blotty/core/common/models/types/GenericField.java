package com.blotty.core.common.models.types;

public interface GenericField {

	public default boolean isNull() {
		return false;
	}
	
	public FieldType getType();
}
