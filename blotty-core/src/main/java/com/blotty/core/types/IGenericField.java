package com.blotty.core.types;

public interface IGenericField {

	public default boolean isNull() {
		return false;
	}
	
	public FieldType getType();

	public int compareTo(IGenericField other);
}
