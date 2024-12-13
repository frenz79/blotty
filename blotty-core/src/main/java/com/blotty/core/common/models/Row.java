package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.impl.NullField;

public class Row {

	private final String key;
	private final GenericField[] fields;
	
	public Row( String key, ColumnsModel colModel ) {
		this.key = key;
		this.fields = new GenericField[ colModel.getColumnsCount() ];
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i] = NullField.NULL;
		}
	}

	public String getKey() {
		return key;
	}

	public GenericField[] getFields() {
		return fields;
	}
	
	public Row set( Column c, GenericField f) throws RowsTypeException {
		if ( !f.isNull() && !f.getType().equals(c.getType()) ) {
			throw new RowsTypeException(String.format("Row with type:%s cannot set with type:%s",c.getType(),f.getType() ));
		}
		this.fields[c.getId()] = f;
		return this;
	}
	
	public Row set( Column c, String str) throws RowsTypeException {
		this.fields[c.getId()] = c.fieldOf( str );
		return this;
	}
	
	public Row clear( Column c ) {
		this.fields[c.getId()] = NullField.NULL;
		return this;
	}
}
