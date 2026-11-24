/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.models.columns;

public class ColumnStatistics {

	private long nullValues = 0;
	private long notNullValues = 0;
	
	public long getNullValues() {
		return nullValues;
	}

	public long getNotNullValues() {
		return notNullValues;
	}

	public void incNullValues() {
		nullValues++;
	}
	
	public void incNotNullValues() {
		notNullValues++;
	}
	
	public void decNullValues() {
		Math.min( 0, --nullValues);
	}
	
	public void decNotNullValues() {
		Math.min( 0, --notNullValues);
	}
	
}
