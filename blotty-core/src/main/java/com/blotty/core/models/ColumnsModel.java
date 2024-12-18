package com.blotty.core.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.types.FieldType;

public class ColumnsModel {

	private final LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
	private List<Column> columnsId = new ArrayList<>();
	
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
			}
			return this;
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
}
