/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.columns;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.StringField;

public final class Column {
	
	private int id = -1;
	private final String name;
	private FieldType type;
	private boolean cacheable;
	private boolean enabledStats;
	private final boolean isKey;
	
	private Map<String,Object> properties;
	private Map<String,? extends IGenericField<?>> valuesCache;
	
	private ColumnStatistics statistics;
	
	public Column( String n, boolean isKey ){
		this.name = n;
		this.isKey = isKey;
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
	
	public IGenericField<?> newField(boolean v) {
		return type.of(v);
	}
	public IGenericField<?> newField(short v) {
		return type.of(v);
	}
	public IGenericField<?> newField(int v) {
		return type.of(v);
	}
	public IGenericField<?> newField(long v) {
		return type.of(v);
	}
	public IGenericField<?> newField(float v) {
		return type.of(v);
	}
	public IGenericField<?> newField(double v) {
		return type.of(v);
	}
	public IGenericField<?> newField(String v) {
		return (isCacheable())
		        ? (StringField) valuesCache.computeIfAbsent(v, StringField::of)
		        : StringField.of(v);
	}
	
	public IGenericField<?> nullField() {
		return type.ofNull();
	}
		
	@Override
	public String toString() {
		return "Column [id=" + id + ", name=" + name + ", type=" + type + "]";
	}

	public boolean isKey() {
		return isKey;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
		if (cacheable) {
			this.valuesCache = new ConcurrentHashMap<>();
		}
	}
	
	public ColumnStatistics getStatistics() {
		return statistics;
	}

	public boolean isEnabledStats() {
		return enabledStats;
	}

	public void setEnabledStats(boolean enabledStats) {
		this.enabledStats = enabledStats;
	}
		
	public static Builder newBuilder(String colName, boolean isKey) {
		return new Builder(colName, isKey);
	}
	
	public static Builder newBuilder(String colName) {
		return new Builder(colName, false);
	}
	
	public static class Builder {
		private final Column col;
		
		public Builder(String colName, boolean isKey ) {
			this.col = new Column(colName, isKey);
		}
		
		public Builder cacheable() {
			this.col.setCacheable(true);
			return this;
		}
		
		public Builder enableStats() {
			this.col.setEnabledStats(true);
			return this;
		}
		
		public Builder disableStats() {
			this.col.setEnabledStats(false);
			return this;
		}
		
		public Builder type(FieldType t) {
			this.col.setType(t);
			this.col.statistics = new ColumnStatistics(t);
			return this;
		}
		
		public Builder id(int id) {
			this.col.setId(id);
			return this;
		}
		
		public Builder withProp(String prop, Object v) {
			if (this.col.properties==null) {
				this.col.properties = new HashMap<>();
			}
			this.col.properties.put(prop, v);
			return this;
		}
		
		public Column build() {
			return col;
		}
	}
}
