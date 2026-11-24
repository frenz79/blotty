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
