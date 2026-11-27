/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.rows.Row;

public class  {

	public static class RowSet {
		private final Set<Row> rows;

		public RowSet(Collection<Row> r) {
			this.rows = new HashSet<Row>(r);
		}
		
		public RowSet() {
			this.rows = new HashSet<Row>();
		}

		public Set<Row> getRows() {
			return rows;
		}

		public void addRow(Row row) {
			rows.add(row);
		}
	}
	
    private final RowSet candidateRows;
    private final IFilterCondition residualFilter;

	public QueryPlan(Collection<Row> rows, IFilterCondition residual) {
		this.candidateRows = new RowSet(rows);
		this.residualFilter = residual;
	}
	
	public QueryPlan(IFilterCondition residual) {
		this.candidateRows = new RowSet();
		this.residualFilter = residual;
	}

	public RowSet getCandidateRows() {
		return candidateRows;
	}

	public IFilterCondition getResidualFilter() {
		return residualFilter;
	}
} 
