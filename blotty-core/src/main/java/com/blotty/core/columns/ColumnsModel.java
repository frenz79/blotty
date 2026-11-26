/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blotty.core.commons.exceptions.ColumnsModelException;

public class ColumnsModel {

	private Map<String,Column> columns = new HashMap<>(64);
	private Column[] columnsId;
	private Column key;
	
	private ColumnsModel(){
	}

	public int getColumnsCount() {
		return columns.size();
	}
	
	public Column getColumn(String name) {
		return columns.get(name);
	}
	
	public Column getColumn(int id) {
		return columnsId[id];
	}
	
	public Collection<Column> getColumns(){
		return columns.values();
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private final ColumnsModel model;
		private final List<Column> columnsId = new ArrayList<>();
		
		public Builder() {
			this.model = new ColumnsModel();
		}
		
		public Builder add( Column.Builder col ) throws ColumnsModelException {
			return add(col.build());
		}
		
		public Builder add( Column col ) throws ColumnsModelException {
			if ( model.columns.containsKey(col.getName())) {
				throw new ColumnsModelException(String.format("Duplicated column name:%s", col.getName()));
			}
			col.setId(model.columns.size());
			model.columns.put(col.getName(), col);
			columnsId.add(col.getId(), col);
			
			if ( col.isKey() ) {
				if (model.key!=null) {
					throw new ColumnsModelException(String.format("More than one columns defined as key"));
				}
				model.key = col;
			}
			return this;
		}
				
		public ColumnsModel build() throws ColumnsModelException{
			if ( model.key==null ) {
				throw new ColumnsModelException(String.format("No key defined"));
			}
			model.columnsId = columnsId.toArray(new Column[0]);			
			return model;
		}
	}

	public Column getKey() {
		return key;
	}
}
