package com.blotty.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.types.IGenericField;

public class SearchIndex {

	private final Map<IGenericField,List<Row>> rowsByIndex = new ConcurrentHashMap<>();
	private final Column column;
	
	private final ReadLock rLock;
	private final WriteLock wlock;
	
	public SearchIndex( Column column ) {
		this.column = column;		
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.rLock = lock.readLock();
		this.wlock = lock.writeLock();
	}
	
	public void build(AbstractRowsModel model) throws RowsModelException {
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
			IGenericField colVal = r.getFields()[column.getId()];				
			rowsByIndex.compute(colVal, (k,v) ->{
				if (v==null) {
					return new ArrayList<>();
				}
				return v;
			}).add(r);
		} finally {
			wlock.unlock();
		}
	}

	public Column getColumn() {
		return column;
	}

	public void stream(IGenericField searchVal, IConsumer<Row> iConsumer) throws RowsModelException {
		rLock.lock();
			try {
			List<Row> rows = rowsByIndex.get(searchVal);
			for ( Row r : rows ) {
				iConsumer.stream(r);
			}
		} finally {
			rLock.unlock();
		}
	}
}
