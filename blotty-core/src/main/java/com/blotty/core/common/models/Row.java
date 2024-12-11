package com.blotty.core.common.models;

import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.types.GenericField;
import com.blotty.core.common.models.types.impl.NullField;

public class Row {

	private final String key;
	private final GenericField[] fields;
	
	public Row( String key, ColumnsModel colModel ) {
		this.key = key;
		this.fields = new GenericField[ colModel.getColumnsCount() ];
	}

	public String getKey() {
		return key;
	}

	public GenericField[] getFields() {
		return fields;
	}
	
	public Row set( Column c, GenericField f) throws RowsTypeException {
		if ( !f.getType().equals(c.getType()) ) {
			throw new RowsTypeException(String.format("Row with type:%s cannot set with type:%s",c.getType(),f.getType() ));
		}
		this.fields[c.getId()] = f;
		return this;
	}
	
	public Row clear( Column c ) {
		this.fields[c.getId()] = NullField.NULL;
		return this;
	}
}
