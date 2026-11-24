package com.blotty.core.models.rows;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.NullField;

public class Row {

	private final IGenericField[] fields;
	
	Row( ColumnsModel colModel ) {
		this.fields = new IGenericField[ colModel.getColumnsCount() ];
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i] = NullField.NULL;
		}
	}

	Row( IGenericField[] fields ) {
		this.fields = fields;
	}
	
	public IGenericField[] getFields() {
		return fields;
	}
	
	public Row set( Column c, IGenericField f) throws RowsTypeException {
		if ( !f.isNull() && !f.getType().equals(c.getType()) ) {
			throw new RowsTypeException(String.format("Row with type:%s cannot set with type:%s",c.getType(),f.getType() ));
		}
		this.fields[c.getId()] = f;
		return this;
	}
	
	public Row clear( Column c ) {
		this.fields[c.getId()] = NullField.NULL;
		return this;
	}
	
	public void upd(Row row) {
		System.arraycopy(row.fields, 0, this.fields, 0, this.fields.length);
	}
	
	public static Builder newBuilder(ColumnsModel colModel) {
		return new Builder(colModel);
	}
	
	public static class Builder {
		private final Row row;
		private final ColumnsModel colModel;
		
		public Builder(ColumnsModel colModel) {
			this.row = new Row(colModel);
			this.colModel = colModel;
		}
		
		public Builder setNull(Column col) throws RowsTypeException {
			this.row.set(col, NullField.NULL);
			return this;
		}
		
		public Builder set(Column col, boolean v) throws RowsTypeException {
			this.row.set(col, col.fieldOf(v));
			return this;
		}
		
		public Builder set(Column col, long v) throws RowsTypeException {
			this.row.set(col, col.fieldOf(v));
			return this;
		}
		
		public Builder set(Column col, double v) throws RowsTypeException {
			this.row.set(col, col.fieldOf(v));
			return this;
		}
		
		public Builder set(Column col, String v) throws RowsTypeException {
			this.row.set(col, col.fieldOf(v));
			return this;
		}
		
		public Builder set(Column col, IGenericField v) throws RowsTypeException {
			this.row.set(col, v);
			return this;
		}
		
		public Builder set(String cName, IGenericField v) throws RowsTypeException {
			this.row.set(colModel.getColumn(cName), v);
			return this;
		}
		
		public Builder set(String cName, String v) throws RowsTypeException {
			Column col = colModel.getColumn(cName);
			this.row.set(col, col.fieldOf(v));
			return this;
		}
		
		public Builder setValues(String ...values) throws RowsTypeException, RowsModelException {
			int len = colModel.getColumnsCount();
			
			if (len!=values.length) {
				throw new RowsModelException("Invalid arguments, expected %d got %d".formatted(len, values.length));
			}
			for (int i=0; i<len; i++) {
				Column col = colModel.getColumn(i);
				this.row.set(col, col.fieldOf(values[i]));
			}			
			return this;
		}
		
		public Builder setValues(IGenericField ...values) throws RowsTypeException, RowsModelException {
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
