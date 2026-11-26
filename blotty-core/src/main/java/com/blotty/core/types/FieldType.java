/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types;

import com.blotty.core.types.impl.BooleanField;
import com.blotty.core.types.impl.DoubleField;
import com.blotty.core.types.impl.FloatField;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.LongField;
import com.blotty.core.types.impl.ShortField;
import com.blotty.core.types.impl.StringField;

public enum FieldType {

	STRING_TYPE(StringField.MIN_VALUE, StringField.MAX_VALUE),
	
	BOOLEAN_TYPE(BooleanField.FALSE, BooleanField.TRUE),
	
	SHORT_TYPE(ShortField.MIN_VALUE, ShortField.MAX_VALUE),
	INTEGER_TYPE(IntegerField.MIN_VALUE, IntegerField.MAX_VALUE),
	LONG_TYPE(LongField.MIN_VALUE, LongField.MAX_VALUE),
	
	FLOAT_TYPE(FloatField.MIN_VALUE, FloatField.MAX_VALUE),
	DOUBLE_TYPE(DoubleField.MIN_VALUE, DoubleField.MAX_VALUE);

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
