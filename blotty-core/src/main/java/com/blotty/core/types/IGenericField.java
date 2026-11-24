/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types;

import java.io.Serializable;

public interface IGenericField extends Serializable {

	public default boolean isNull() {
		return false;
	}
	
	public FieldType getType();

	public int compareTo(IGenericField other);
}
