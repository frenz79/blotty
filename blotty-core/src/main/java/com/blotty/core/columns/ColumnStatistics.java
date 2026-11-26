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

	private static final int DIRTY_VALUE = Integer.MIN_VALUE;
	private static final int FOOTPRINT_SIZE = 512; // Pow2!
	private static final int DISTRIBUTION_SIZE = 32; // Pow2!
	
	private int totValues = 0;
	private int nullValues = 0;
	private IGenericField<?> maxValue;
	private IGenericField<?> minValue;
	private int maxCount = 0;
	private int minCount = 0;	
	private final BitSet footprint = new BitSet(FOOTPRINT_SIZE);
	private final int[] distribution = new int[DISTRIBUTION_SIZE];
		
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
			this.footprint.set( hashCode & (FOOTPRINT_SIZE-1), set );
		}
	}
	
	public void add ( IGenericField<?> val ) {
		totValues++;
		if (val.isNull()) {
			nullValues++;
			return;
		}
		
		setUnsetDistributionStats(val, true);
		
		// first ins
		if (getNotNullValues()==1) {
			maxValue = val;
			maxCount = 1;	
			minValue = val;
			minCount = 1;
			return;
		}
		
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
		
		if (maxCount!=DIRTY_VALUE && val.compareGeneric(maxValue) == 0 && maxCount>0) {
			maxCount--;
			if (maxCount==0) maxCount = DIRTY_VALUE;
			return;
		} 
		if (minCount!=DIRTY_VALUE && val.compareGeneric(minValue) == 0 && minCount>0) {
			minCount--;
			if (minCount==0) minCount = DIRTY_VALUE;
			return;
		}
	}
	
	public void upd ( IGenericField<?> newV, IGenericField<?> oldV ) {
		if(newV.compareGeneric(oldV)==0) {
			return;
		}
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
		
		if (maxCount!=DIRTY_VALUE) { 
			if(newV.compareGeneric(maxValue)>0) {
				maxCount = 1;
				maxValue = newV;
				return;
			}
			if (newV.compareGeneric(maxValue)<0 && oldV.compareGeneric(maxValue)==0) {
				maxCount--;
				if (maxCount==0) {
					maxCount = DIRTY_VALUE;
				}
				return;
			}
		}		
		if (minCount!=DIRTY_VALUE) {
			if (newV.compareGeneric(minValue)<0) {
				minCount = 1;
				minValue = newV;
				return;
			} 
			if (newV.compareGeneric(minValue)>0 && oldV.compareGeneric(minValue)==0) {
				minCount--;
				if (minCount==0) {
					minCount = DIRTY_VALUE;
				}
				return;
			}
		}
	}
	
	public double getBucketOccupancy() {
		int used = 0;
		for (int v:distribution) {
			if (v>0) used++;
		}
		return used / (double)DISTRIBUTION_SIZE;
	}
	
	/**
	 * smaller(<0.1) -> low variability
	 * higher (>0.9) -> lots of different values 
	 */
	public double getDistinctness() {
		return ((double)footprint.cardinality()) / FOOTPRINT_SIZE;
	}

	/**
	 * Most populated bucket
	 * smaller(<0.1) -> high distribution
	 * higher (>0.9) -> high concentration
	 */
	public double getDominanceRatio() {
		int notNull = getNotNullValues();
		if (notNull==0) return 0.0d;
		
		int max = 0;
		for (int v:distribution) {
			if (v>max) max=v;
		}
		return ((double)max)/notNull;
	}
	
	/**
	 * Measures values distribution
	 * smaller(<0.1) -> high concentration
	 * higher (>0.9) -> high uniformity
	 */
	public double getUniformityScore() {
		if (getNotNullValues()==0) return 0.0d;
		
		double mean = getNotNullValues() / (double)DISTRIBUTION_SIZE;
		double deviation = 0.0d;
		for (int v: distribution) {
			deviation += Math.abs(v-mean);
		}
		return 1.0d - ((deviation/((double)DISTRIBUTION_SIZE))/mean);
	}

	public double getMinFrequency() {
		int notNull = getNotNullValues();
		return notNull==0?0:((double)minCount)/notNull;
	}
	
	public double getMaxFrequency() {
		int notNull = getNotNullValues();
		return notNull==0?0:((double)maxCount)/notNull;
	}
	
	/**
	 * Measures values distribution
	 * smaller(<0.1) -> same value not frequent
	 * higher (>0.9) -> same value very frequent
	 */
	public double getCompressionPotential() {
		return 1.0 - getDistinctness();
	}
	
	/**
	 * Measures if index is useful
	 * smaller(<0.1) -> useless
	 * higher (>0.9) -> yeah!
	 */
	public double getIndexAffinity() {
		return getDistinctness() * getBucketOccupancy();
	}
	
	public double getSparsity() {
		return 1.0 - getNotNullRatio();
	}
	
	public double getPlannerScore() {
		return Math.sqrt(getNotNullRatio() * getDistinctness() * getUniformityScore());
	}
	
	public double getNullRatio() {
	    return totValues == 0 ? 0 : ((double) nullValues) / totValues;
	}
	
	public double getNotNullRatio() {
	    return totValues == 0 ? 0 : ((double) (totValues-nullValues)) / totValues;
	}
	
	public int getNotNullValues() {
		return totValues-nullValues;
	}
	
	public int getNullValues() {
		return nullValues;
	}

	public long getTotalValues() {
	    return totValues;
	}

	public boolean isDirtyMax() {
		return maxCount==DIRTY_VALUE;
	}

	public boolean isDirtyMin() {
		return minCount==DIRTY_VALUE;
	}

	public IGenericField<?> getMaxValue() {
		return maxValue;
	}
	
	public IGenericField<?> getMinValue() {
		return minValue;
	}
}
