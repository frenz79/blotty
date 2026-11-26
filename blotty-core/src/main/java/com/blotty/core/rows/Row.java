/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.rows;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;

public class Row {

	private final IGenericField<?>[] fields;
	
	Row( ColumnsModel colModel ) throws RowsTypeException {
		this.fields = new IGenericField[ colModel.getColumnsCount() ];
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i] = colModel.getColumn(i).nullField();
		}
	}

	Row( IGenericField<?>[] fields ) {
		this.fields = fields;
	}
	
	public static record RowChange(
		BitSet modifiedCols,
		List<IGenericField<?>> oldValues
	) {
		public boolean isModified( int columnId ) {
			return modifiedCols.get(columnId);
		}
		
		// Count number of set bits before colId
		public int resolveId(int colId) {
			int counter = 0;
			for (int i=0; i<colId;i++) {
				if (modifiedCols.get(i)) {
					counter++;
				}
			}
			return counter;
		}
	}
	
	public IGenericField<?>[] getFields() {
		return fields;
	}
	
	public RowChange upd(Row row, boolean trackChanges ) {
		if (!trackChanges) {
			System.arraycopy(row.fields, 0, this.fields, 0, this.fields.length);
			return null;
		}
		
		BitSet modifiedCols = new BitSet(fields.length);
		List<IGenericField<?>> oldValues = new ArrayList<>();
		for (int i=0; i<fields.length; i++) {
			if (fields[i].compareGeneric(row.fields[i])!=0) {
				modifiedCols.set(i, true);
				oldValues.add(fields[i]);
				fields[i] = row.fields[i];
			}
		}
		
		return new RowChange(modifiedCols, oldValues);
	}
	
	public Row set( Column c, IGenericField<?> f) throws RowsTypeException {
		if ( !f.isNull() && !f.getType().equals(c.getType()) ) {
			throw new RowsTypeException(String.format("Row with type:%s cannot set with type:%s",c.getType(),f.getType() ));
		}
		this.fields[c.getId()] = f;
		return this;
	}
	
	public Row clear( Column c ) throws RowsTypeException {
		this.fields[c.getId()] = c.nullField();
		return this;
	}
	
	public static Builder newBuilder(ColumnsModel colModel) throws RowsTypeException {
		return new Builder(colModel);
	}
	
	public static class Builder {
		private final Row row;
		private final ColumnsModel colModel;
		
		public Builder(ColumnsModel colModel) throws RowsTypeException {
			this.row = new Row(colModel);
			this.colModel = colModel;
		}
		
		public Builder setNull(Column col) throws RowsTypeException {
			this.row.set(col, col.nullField());
			return this;
		}
		
		public Builder set(Column col, boolean v) throws RowsTypeException {
			this.row.set(col, col.newField(v));
			return this;
		}
		
		public Builder set(Column col, long v) throws RowsTypeException {
			this.row.set(col, col.newField(v));
			return this;
		}
		
		public Builder set(Column col, double v) throws RowsTypeException {
			this.row.set(col, col.newField(v));
			return this;
		}
		
		public Builder set(Column col, String v) throws RowsTypeException {
			this.row.set(col, col.newField(v));
			return this;
		}
		
		public Builder set(Column col, IGenericField<?> v) throws RowsTypeException {
			this.row.set(col, v);
			return this;
		}
		
		public Builder set(String cName, IGenericField<?> v) throws RowsTypeException {
			this.row.set(colModel.getColumn(cName), v);
			return this;
		}
		
		public Builder set(String cName, String v) throws RowsTypeException {
			Column col = colModel.getColumn(cName);
			this.row.set(col, col.newField(v));
			return this;
		}
		
		public Builder setValues(String ...values) throws RowsTypeException, RowsModelException {
			int len = colModel.getColumnsCount();
			
			if (len!=values.length) {
				throw new RowsModelException("Invalid arguments, expected %d got %d".formatted(len, values.length));
			}
			for (int i=0; i<len; i++) {
				Column col = colModel.getColumn(i);
				this.row.set(col, col.newField(values[i]));
			}			
			return this;
		}
		
		public Builder setValues(IGenericField<?> ...values) throws RowsTypeException, RowsModelException {
			int len = colModel.getColumnsCount();
			
			if (len!=values.length) {
				throw new RowsModelException("Invalid arguments, expected %d got %d".formatted(len, values.length));
			}
			for (int i=0; i<len; i++) {
				Column col = colModel.getColumn(i);
				this.row.set(col, values[i]);
			}			
			return this;
		}
		
		public Row build() {
			return row;
		}	
	}
}
