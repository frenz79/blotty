/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.sorters;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.AbstractRowsModel;
import com.blotty.core.models.rows.Row;
import com.blotty.core.types.IGenericField;

public class SorterConditionChain implements ISorterCondition, Comparator<Row> {

	private final List<SorterExpression> sorters = new LinkedList<>();
	
	@Override
	public boolean apply(AbstractRowsModel rows) {
		try {
			rows.sort(this);
			return true;
		} catch (RowsModelException e) {
			throw new RuntimeException("Error during sorting", e);
		}
	}
	
	@Override
	public int compare(Row r1, Row r2) {
		for (SorterExpression sorter : sorters) {
			int colId = sorter.getColumm().getId();
			IGenericField f1 = r1.getFields()[colId];
			IGenericField f2 = r2.getFields()[colId];
			int comp = sorter.getOperator().compare(f1, f2);
			if (comp != 0) {
				return comp;
			}
		}
		return 0;
	}
	
	public boolean addSorter( SorterExpression expr ) {
		return sorters.add(expr);
	}

	public List<SorterExpression> getSorters() {
		return sorters;
	}

	@Override
	public String toString() {
		return "SorterConditionChain [sorters=" + sorters + "]";
	}

}
