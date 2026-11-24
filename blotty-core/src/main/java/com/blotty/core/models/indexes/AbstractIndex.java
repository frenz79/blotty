/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.models.indexes;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.AbstractRowsModel;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.rows.Row;
import com.blotty.core.models.rows.Row.RowChange;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.types.IGenericField;

public abstract class AbstractIndex<S extends IndexStatistics> {

	private final Column column;
	private final S statistics;
	
	final ReadLock rLock;
	final WriteLock wlock;
	
	public abstract boolean supports( IFilterCondition c );
	public abstract Collection<Row> lookup( IFilterCondition c );
	protected abstract void _insert(IGenericField key, Row r);
	protected abstract void _delete(IGenericField key, Row r);
	
	public long estimatedCardinality(IFilterCondition c) {
		return lookup(c).size();
	}
	
	public AbstractIndex(Column column, S statistics) {
		super();
		this.column = column;
		this.statistics = statistics;
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
					_insert(getKey(r), r);
					return true;
				}
			});
		} finally {
			wlock.unlock();
		}
	}

	public Column getColumn() {
		return column;
	}
	
	public void update(Row r, RowChange change) {
		wlock.lock();
		try {
			int colId = getColumn().getId();
			
			if ( change.modifieldCols().get(colId) ) {
				IGenericField oldK = r.getFields()[colId];
				IGenericField newK = change.oldValues().get(change.resolveId(colId));
				
				if (oldK.equals(newK)) {
					// Should not happen!
					return;
				}
				// TODO: _delete( oldK, oldR );
				// TODO: _insert( newK, newR );
			}			
		} finally {
			wlock.unlock();
		}
	}
	
	public void delete(Row r) {
		wlock.lock();
		try {			
			_delete( getKey(r), r );
		} finally {
			wlock.unlock();
		}
	}	
	
	public void insert(Row r) {
		wlock.lock();
		try {			
			_insert( getKey(r), r );
		} finally {
			wlock.unlock();
		}
	}
	
	private IGenericField getKey(Row r) {
		return r.getFields()[getColumn().getId()];
	}
	public S getStatistics() {
		return statistics;
	}
}
