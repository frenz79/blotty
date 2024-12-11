package com.blotty.core.common.models;

import java.util.LinkedHashMap;

import com.blotty.core.common.exceptions.ColumnsModelException;

public class ColumnsModel {

	private final LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
	
	private ColumnsModel(){
		
	}
	
	public int getColumnsCount() {
		return columns.size();
	}
	
	public Column getColumn(String name) {
		return columns.get(name);
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
			return this;
		}
		
		public ColumnsModel build(){
			return model;
		}
	}
}
