/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions.binary.operators;

import com.blotty.core.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.StringField;

public class Like extends IBinaryOperator {
	
	@Override
	public boolean apply(IGenericField<?> f1, IGenericField<?> f2) {
	    if (f1.isNull() || f2.isNull()) return false;
	    return ((StringField)f1).contains((StringField)f2);
	}
}
