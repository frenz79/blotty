package com.blotty.core.types;

import java.io.Serializable;

public interface IGenericField extends Serializable {

	public default boolean isNull() {
		return false;
	}
	
	public FieldType getType();

	public int compareTo(IGenericField other);
}
