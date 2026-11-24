/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions.binary;

import com.blotty.core.modifiers.filters.conditions.IFilterOperator;
import com.blotty.core.types.IGenericField;

public abstract class IBinaryOperator implements IFilterOperator {

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public abstract boolean apply(IGenericField f1, IGenericField f2);

}
