package com.blotty.core.common.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.blotty.core.common.exceptions.ColumnsModelException;
import com.blotty.core.common.models.types.FieldType;

public class ColumnsModel {

	private final LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
	private List<Column> columnsId = new ArrayList<>();
	
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
			return this;
		}
		
		public ColumnsModelBuilder add( String colName, FieldType type ) throws ColumnsModelException {
			return add(new Column(colName, type));
		}
		
		public ColumnsModel build(){
			return model;
		}
	}
}
