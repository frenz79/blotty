/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions.unary.operators;

import com.blotty.core.modifiers.filters.conditions.unary.IUnaryOperator;
import com.blotty.core.types.IGenericField;

public class IsNull extends IUnaryOperator {

	@Override
	public boolean apply(IGenericField<?> f) {
		return f.isNull();
	}

}
