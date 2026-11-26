/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.columns;

import java.util.BitSet;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class ColumnStatistics {

	private long nullValues = 0;
	private long notNullValues = 0;
	private IGenericField<?> maxValue;
	private IGenericField<?> minValue;
	private boolean dirtyMax = false;
	private boolean dirtyMin = false;
	private int maxCount = 0;
	private int minCount = 0;	
	private final BitSet footprint = new BitSet(512);
	private final int[] distribution = new int[32];
		
	public ColumnStatistics( FieldType colType ) {
		this.maxValue = colType.getDefaultMax();
		this.minValue = colType.getDefaultMin();
	}
	
	public void add ( IGenericField<?> val) {
		if (val.isNull()) {
			nullValues++;
			return;
		}
		notNullValues++;
		int cmpMax = val.compareGeneric(maxValue);
		if (cmpMax == 0) {
			maxCount++;
		} else if (cmpMax>0) {
			maxValue = val;
			maxCount = 1;
		} else {
			int cmpMin = val.compareGeneric(minValue);
			if (cmpMin==0) {		
				minCount++;
			} else if (cmpMin<0) {
				minValue = val;
				minCount = 1;
			}
		}		
	}
	
	public void del ( IGenericField<?> v) {
		
	}
	
	public void upd ( IGenericField<?> v) {
		
	}
	

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
	    if (nullValues > 0) {
	        nullValues--;
	    }
	}
	
	public void decNotNullValues() {
	    if (notNullValues > 0) {
	        notNullValues--;
	    }
	}

	public long getTotalValues() {
	    return nullValues + notNullValues;
	}

	public double getNullRatio() {
	   	long total = getTotalValues();
	    return total == 0 ? 0 : ((double) nullValues) / total;
	}
}
