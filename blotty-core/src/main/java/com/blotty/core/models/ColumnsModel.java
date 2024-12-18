package com.blotty.core.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.types.FieldType;

public class ColumnsModel {

	private final LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
	private List<Column> columnsId = new ArrayList<>();
	
	private Column key;
	private boolean hasAutoKey = false;
	private AtomicLong autoKeyValue = new AtomicLong(0);
	
	private ColumnsModel(){
		
	}
	
	public int getColumnsCount() {
		return columns.size();
	}
	
	public Column getColumn(String name) {
		return columns.get(name);
	}
	
	public Column getColumn(int id) {
		return columnsId.get(id);
	}
	
	public Collection<Column> getColumns(){
		return columns.values();
	}
	
	public static class ColumnsModelBuilder {
		
		private final ColumnsModel model;
		
		public ColumnsModelBuilder() {
			this.model = new ColumnsModel();
		}
		
		public ColumnsModelBuilder add( Column col ) throws ColumnsModelException {
			if ( model.columns.containsKey(col.getName())) {
				throw new ColumnsModelException(String.format("Duplicated column name:%s", col.getName()));
			}
			col.setId(model.columns.size());
			model.columns.put(col.getName(), col);
			model.columnsId.add(col.getId(), col);
			
			if ( col.isKey() ) {
				if (model.key!=null) {
					throw new ColumnsModelException(String.format("More than one columns defined as key"));
				}
				model.key = col;
				model.hasAutoKey = col.isAutoKey();
			}
			return this;
		}
		
		public ColumnsModelBuilder addAutoKey( String colName ) throws ColumnsModelException {
			return add(new Column(colName, true, true, FieldType.LONG_TYPE));
		}
		
		public ColumnsModelBuilder addKey( String colName, FieldType type ) throws ColumnsModelException {
			return add(new Column(colName, true, type));
		}
		
		public ColumnsModelBuilder add( String colName, FieldType type ) throws ColumnsModelException {
			return add(new Column(colName, type));
		}
		
		public ColumnsModel build() throws ColumnsModelException{
			if ( model.key==null ) {
				throw new ColumnsModelException(String.format("No key defined"));
			}
			return model;
		}
	}

	public Column getKey() {
		return key;
	}

	public boolean hasAutoKey() {
		return hasAutoKey;
	}

	public long getAutoKeyValue() {
		return autoKeyValue.incrementAndGet();
	}
}
