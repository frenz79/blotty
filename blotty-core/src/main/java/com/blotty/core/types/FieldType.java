/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types;

import com.blotty.core.types.impl.NullField;

public enum FieldType {

	NULL(NullField.),
	STRING_TYPE,
	
	BOOLEAN_TYPE,
	
	SHORT_TYPE,
	INTEGER_TYPE,
	LONG_TYPE,
	
	FLOAT_TYPE,
	DOUBLE_TYPE;

	private final IGenericField<?> min;
	private final IGenericField<?> man;
	
	public IGenericField<?> getDefaultMax() {
		return man;
	}

	public IGenericField<?> getDefaultMin() {
		return min;
	}

	private FieldType(IGenericField<?> min, IGenericField<?> man) {
		this.min = min;
		this.man = man;
	}	
}
