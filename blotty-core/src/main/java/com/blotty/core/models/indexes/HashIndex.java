package com.blotty.core.models.indexes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.Column;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.types.IGenericField;

public class HashIndex extends AbstractIndex {

	private final Map<IGenericField,Set<Row>> index = new HashMap<>();
		
	public HashIndex( Column column ) {
		super(column);
	}

	public void stream(IGenericField searchVal, IConsumer<Row> iConsumer) throws RowsModelException, FilterExpressionException {
		rLock.lock();
			try {
			Set<Row> rows = index.get(searchVal);
			for ( Row r : rows ) {
				iConsumer.stream(r);
			}
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public boolean supports(IFilterCondition c) {
		if (!(c instanceof BinaryCondition bc)) {
			return false;
		}
		if (!(bc.getOperator() instanceof Equal)) {
			return false;
		}
		if (!(bc.getLeft().isColumn())) {
			return false;
		}
		if (!(bc.getRight().isConst())) {
			return false;
		}
		return bc.getLeft().getColumn().equals(getColumn());
	}

	@Override
	public Collection<Row> lookup(IFilterCondition c) {
		BinaryCondition bc = (BinaryCondition)c;
		Collection<Row> rows = index.get(bc.getRight().getConst());
		return rows==null?List.of():rows;
	}

	@Override
	protected void _insert(IGenericField key, Row r) {
		index.compute(key, (k,v) ->{
			if (v==null) {
				return new HashSet<>();
			}
			return v;
		}).add(r);
	}

	@Override
	protected void _delete(IGenericField key, Row r) {
		Set<Row> rows = index.get(key);
		if (rows==null || rows.isEmpty()) {
			index.remove(key);
			return;
		}
		rows.remove(r);
		if (rows.isEmpty()) {
			index.remove(key);
		}		
	}
}
