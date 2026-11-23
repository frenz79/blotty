package com.blotty.core.models.indexes;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.AbstractRowsModel;
import com.blotty.core.models.Column;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.types.IGenericField;

public class HashIndex extends AbstractIndex {

	private final Map<IGenericField,Set<Row>> index = new ConcurrentHashMap<>();
	
	private final ReadLock rLock;
	private final WriteLock wlock;
	
	public HashIndex( Column column ) {
		super(column);
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.rLock = lock.readLock();
		this.wlock = lock.writeLock();
	}
	
	public void build(AbstractRowsModel model) throws RowsModelException, FilterExpressionException {
		wlock.lock();
		try {
			model.stream( new IConsumer<Row>() {			
				@Override
				public boolean stream(Row r) throws RowsModelException {
					add(r);
					return true;
				}
			});
		} finally {
			wlock.unlock();
		}
	}
	
	public void update(Row r) {
		add(r);
	}
	
	private void add(Row r) {
		wlock.lock();
		try {
			IGenericField colVal = r.getFields()[getColumn().getId()];				
			index.compute(colVal, (k,v) ->{
				if (v==null) {
					return new HashSet<>();
				}
				return v;
			}).add(r);
		} finally {
			wlock.unlock();
		}
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
}
