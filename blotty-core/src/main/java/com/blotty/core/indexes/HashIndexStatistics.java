/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.indexes;

public class HashIndexStatistics implements IndexStatistics {

    private long distinctValues = 0;
	private long largestBucket = 0;
	
	public long getDistinctValues() {
		return distinctValues;
	}

	public void setDistinctValues(long distinctValues) {
		this.distinctValues = distinctValues;
	}

}
