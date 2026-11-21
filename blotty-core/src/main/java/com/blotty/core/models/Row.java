package com.blotty.core.models;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.NullField;

public class Row {

	private final IGenericField[] fields;
	
	public Row( ColumnsModel colModel ) {
		this.fields = new IGenericField[ colModel.getColumnsCount() ];
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i] = NullField.NULL;
		}
	}

	public Row( IGenericField[] fields ) {
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
	
	public Row set( Column c, short v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row set( Column c, boolean v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row set( Column c, float v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row set( Column c, double v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row set( Column c, long v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row set( Column c, int v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
		
	public Row set( Column c, String v) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( v );
		return this;
	}
	
	public Row clear( Column c ) {
		this.fields[c.getId()] = NullField.NULL;
		return this;
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
		
		public Builder set(String cName, IGenericField v) throws RowsTypeException {
			this.row.set(colModel.getColumn(cName), v);
			return this;
		}
		
		public Builder set(String cName, String v) throws RowsTypeException {
			this.row.set(colModel.getColumn(cName), v);
			return this;
		}
		
		public Builder setValues(String ...values) throws RowsTypeException, RowsModelException {
			int len = colModel.getColumnsCount();
			
			if (len!=values.length) {
				throw new RowsModelException("Invalid arguments, expected %d got %d".formatted(len, values.length));
			}
			for (int i=0; i<len; i++) {
				this.row.set(colModel.getColumn(i), values[i]);
			}			
			return this;
		}
		
		public Row build() {
			return row;
		}		
	}
}
