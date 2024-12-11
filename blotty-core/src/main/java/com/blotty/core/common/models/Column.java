package com.blotty.core.common.models;

import java.util.HashMap;
import java.util.Map;

import com.blotty.core.common.models.types.FieldType;

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
}
