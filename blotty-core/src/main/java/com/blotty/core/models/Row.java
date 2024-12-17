package com.blotty.core.models;

import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.NullField;

public class Row {

	private final String key;
	private final IGenericField[] fields;
	
	public Row( String key, ColumnsModel colModel ) {
		this.key = key;
		this.fields = new IGenericField[ colModel.getColumnsCount() ];
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i] = NullField.NULL;
		}
	}

	public String getKey() {
		return key;
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
}
