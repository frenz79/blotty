/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.sorters;

import com.blotty.core.AbstractRowsModel;

public interface ISorterCondition {

	public boolean apply( AbstractRowsModel rows );
}
