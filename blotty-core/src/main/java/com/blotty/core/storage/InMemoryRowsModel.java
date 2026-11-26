/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.blotty.core.AbstractRowsModel;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.rows.Row;
import com.blotty.core.rows.Row.RowChange;
import com.blotty.core.types.IGenericField;

public class InMemoryRowsModel extends AbstractRowsModel {

	private final Map<IGenericField<?>,Row> rowsByKey = new HashMap<>();
	private final List<Row> rows = new ArrayList<>();

	private final ReadLock rLock;
	private final WriteLock wlock;

	public InMemoryRowsModel(ColumnsModel columnsModel) {
		super( columnsModel );
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.rLock = lock.readLock();
		this.wlock = lock.writeLock();
	}

	@Override
	public void delRow( Row row ) throws RowsModelException {
		IGenericField<?> key = getKey(row);
		wlock.lock();
		try {
			if ( !this.rowsByKey.containsKey(key) ) {
				throw new RowsModelException(String.format("Cannot find row with key:%s", key));
			}
			this.rowsByKey.remove(key);
			this.rows.remove(row);
		} finally {
			wlock.unlock();
		}
	}
	
	@Override
	public RowChange updRow( Row row, boolean trackChanges ) throws RowsModelException {
		IGenericField<?> key = getKey(row);
		wlock.lock();
		try {
			Row modelRow = rowsByKey.get(key);
			if (modelRow==null) {
				throw new RowsModelException(String.format("Cannot find row with key:%s", key));
			}
			return modelRow.upd(row, trackChanges);
		} finally {
			wlock.unlock();
		}
	}
	
	@Override
	public void addRow( Row row ) throws RowsModelException {
		IGenericField<?> key = getKey(row);
		wlock.lock();
		try {
			if ( this.rowsByKey.containsKey(key) ) {
				throw new RowsModelException(String.format("Duplicated key:%s", key));
			}
			this.rowsByKey.put(key, row);
			this.rows.add(row);
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public void addRows( Row ...rows ) throws RowsModelException {
		wlock.lock();
		try {
			for ( Row row : rows ) {
				IGenericField<?> key = getKey(row);
				if ( this.rowsByKey.containsKey(key) ) {
					throw new RowsModelException(String.format("Duplicated key:%s", key));
				}
				this.rowsByKey.put(key, row);
				this.rows.add(row);
			}
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public int getRowsCount() {
		rLock.lock();
		try {
			return rows.size();
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public void stream( IConsumer<Row> c ) throws RowsModelException, FilterExpressionException {
		rLock.lock();
		try {
			for (Row row : rows) {
				if (!c.stream(row)) break;
			}
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public Row getRow(int position) {
		rLock.lock();
		try {
			return rows.get(position);
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public Row getRow(IGenericField<?> key) {
		rLock.lock();
		try {
			return rowsByKey.get(key);
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public void sort(Comparator<Row> comparator) {
		wlock.lock();
		try {
			rows.sort(comparator);
		} finally {
			wlock.unlock();
		}
	}
}
