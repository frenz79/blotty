package com.blotty.core.modifiers.sorters.operators;

import com.blotty.core.types.IGenericField;

public class DescNullFirst implements ISorterOperator {

	@Override
	public int compare(IGenericField o1, IGenericField o2) {
		if ( o1.isNull() ) {
			return -1;
		}
		if ( o2.isNull() ) {
			return -1;
		}
		return o2.compareTo(o1);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
