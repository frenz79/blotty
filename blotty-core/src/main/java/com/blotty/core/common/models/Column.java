package com.blotty.core.common.models;

import java.util.HashMap;
import java.util.Map;

import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.impl.NullField;
import com.blotty.core.common.models.types.impl.StringField;

public class Column {
	
	private int id = -1;
	private final String name;
	private final FieldType type;
	private final boolean cacheable;
	private final Map<String,Object> properties = new HashMap<>();
	
	public Column( String n, FieldType type ){
		this.name = n;
		this.type = type;
		this.cacheable = false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public boolean isCacheable() {
		return cacheable;
	}
	
	public Object getProperty( String k ) {
		return properties.get(k);
	}
	
	public void setProperty( String k, Object v ) {
		properties.put(k, v);
	}

	public FieldType getType() {
		return type;
	}

	public GenericField fieldOf(String str) throws RowsTypeException {
		switch ( type ) {
			case NULL: return NullField.NULL;
			case STRING_TYPE: return StringField.of(str);		
		}
		throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
	}
}
