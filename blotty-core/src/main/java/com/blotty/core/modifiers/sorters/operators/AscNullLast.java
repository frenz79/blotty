package com.blotty.core.modifiers.sorters.operators;

import com.blotty.core.types.IGenericField;

public class AscNullLast implements ISorterOperator {

	@Override
	public int compare(IGenericField o1, IGenericField o2) {
		if ( o1.isNull() ) {
			return -1;
		}
		if ( o2.isNull() ) {
			return -1;
		}
		return o1.compareTo(o2);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
