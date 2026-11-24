package com.blotty.core.models.indexes;

public class HashIndexStatistics implements IndexStatistics {

    private long distinctValues = 0;

	public long getDistinctValues() {
		return distinctValues;
	}

	public void setDistinctValues(long distinctValues) {
		this.distinctValues = distinctValues;
	}

}
