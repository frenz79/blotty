package com.blotty.core.models.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;

public class QueryPlan {

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
