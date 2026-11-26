/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.columns;

import java.util.Arrays;
import java.util.BitSet;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;

public class ColumnStatistics {

	private static final int DIRTY_COUNT = Integer.MIN_VALUE;
	
	private long totValues = 0;
	private long nullValues = 0;
	private IGenericField<?> maxValue;
	private IGenericField<?> minValue;
	private int maxCount = 0;
	private int minCount = 0;	
	private final BitSet footprint = new BitSet(512);
	private final int[] distribution = new int[32];
		
	private final IGenericField<?> defaultMaxValue;
	private final IGenericField<?> defaultMinValue;
	
	public ColumnStatistics( FieldType colType ) {
		// init max with min value and vice-versa
		this.defaultMaxValue = colType.getDefaultMin();
		this.defaultMinValue = colType.getDefaultMax();
		reset();
	}
	
	public void reset() {
		this.totValues = 0;
		this.nullValues = 0;
		this.maxCount = 0;
		this.minCount = 0;
		this.maxValue = defaultMaxValue;
		this.minValue = defaultMinValue;
		Arrays.fill(distribution, 0);
		footprint.clear();
	}
	
	private void setUnsetDistributionStats(IGenericField<?> val, boolean set) {
		int hashCode = val.hashCode();
		this.distribution[ hashCode & 31 ] += (set)?1:-1;
		if (set) {
			this.footprint.set( hashCode & 511, set );
		}
	}
	
	public void add ( IGenericField<?> val ) {
		totValues++;
		if (val.isNull()) {
			nullValues++;
			return;
		}
		
		setUnsetDistributionStats(val, true);

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
	
	public void del ( IGenericField<?> val) {
		totValues--;
		if (val.isNull()) {
			if (nullValues>0) {
				nullValues--;
			}
			return;
		}
		
		setUnsetDistributionStats(val, false);
		
		if (maxCount!=DIRTY_COUNT && val.compareGeneric(maxValue) == 0 && maxCount>0) {
			maxCount--;
			if (maxCount==0) maxCount = DIRTY_COUNT;
			return;
		} 
		if (minCount!=DIRTY_COUNT && val.compareGeneric(minValue) == 0 && minCount>0) {
			minCount--;
			if (minCount==0) minCount = DIRTY_COUNT;
			return;
		}
	}
	
	public void upd ( IGenericField<?> newV, IGenericField<?> oldV ) {
		if (newV.isNull() && !oldV.isNull()) {
			nullValues++;
			setUnsetDistributionStats(oldV, false);
			return;
		} 
		if (!newV.isNull() && oldV.isNull()) {
			nullValues--;
			setUnsetDistributionStats(newV, true);
			return;
		}
		
		setUnsetDistributionStats(oldV, false);
		setUnsetDistributionStats(newV, true);
		
		if (maxCount!=DIRTY_COUNT) { 
			if(newV.compareGeneric(maxValue)>0) {
				maxCount = 1;
				maxValue = newV;
				return;
			}
			if (newV.compareGeneric(maxValue)<0 && oldV.compareGeneric(maxValue)==0) {
				maxCount--;
				if (maxCount==0) {
					maxCount = DIRTY_COUNT;
				}
				return;
			}
		}		
		if (minCount!=DIRTY_COUNT) {
			if (newV.compareGeneric(minValue)<0) {
				minCount = 1;
				minValue = newV;
				return;
			} 
			if (newV.compareGeneric(minValue)>0 && oldV.compareGeneric(minValue)==0) {
				minCount--;
				if (minCount==0) {
					minCount = DIRTY_COUNT;
				}
				return;
			}
		}
	}
	
	public double getHashDensity() {
		return ((double)footprint.cardinality()) / footprint.size();
	}
	
	public long getNotNullValues() {
		return totValues-nullValues;
	}
	
	public long getNullValues() {
		return nullValues;
	}

	public long getTotalValues() {
	    return totValues;
	}

	public double getNullRatio() {
	    return totValues == 0 ? 0 : ((double) nullValues) / totValues;
	}

	public boolean isDirtyMax() {
		return maxCount==DIRTY_COUNT;
	}

	public boolean isDirtyMin() {
		return minCount==DIRTY_COUNT;
	}

	public IGenericField<?> getMaxValue() {
		return maxValue;
	}
	
	public IGenericField<?> getMinValue() {
		return minValue;
	}
}
