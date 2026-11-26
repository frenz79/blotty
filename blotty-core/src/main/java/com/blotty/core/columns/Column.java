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

import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.BooleanField;
import com.blotty.core.types.impl.DoubleField;
import com.blotty.core.types.impl.FloatField;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.LongField;
import com.blotty.core.types.impl.ShortField;
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
	
	private StringField from( String str ) {
	    return (isCacheable())
	        ? (StringField) valuesCache.computeIfAbsent(str, StringField::of)
	        : StringField.of(str);
	}
	
	public IGenericField<?> fieldNull() throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return StringField.NULL;
		case INTEGER_TYPE: return IntegerField.NULL;
		case LONG_TYPE:  return LongField.NULL;
		case BOOLEAN_TYPE: return BooleanField.NULL;
		case DOUBLE_TYPE: return DoubleField.NULL;
		case FLOAT_TYPE: return FloatField.NULL;
		case SHORT_TYPE: return ShortField.NULL;
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}
	
	public IGenericField<?> fieldOf(String v) throws RowsTypeException {
		if (v==null || v.length()==0 ) {
			return fieldNull();
		}
		switch ( type ) {
		case STRING_TYPE: return from(v);
		case INTEGER_TYPE: return IntegerField.of(v);
		case LONG_TYPE:  return LongField.of(v);
		case BOOLEAN_TYPE: return BooleanField.of(v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of(v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}		
	}
	
	public IGenericField<?> fieldOf(long v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
		case INTEGER_TYPE: return IntegerField.of((int)v);
		case LONG_TYPE:  return LongField.of(v);
		case BOOLEAN_TYPE: return BooleanField.of((int)v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}
	
	public IGenericField<?> fieldOf(int v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
		case INTEGER_TYPE: return IntegerField.of(v);
		case LONG_TYPE:  return LongField.of((long)v);
		case BOOLEAN_TYPE: return BooleanField.of(v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}	

	public IGenericField<?> fieldOf(float v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
		case INTEGER_TYPE: return IntegerField.of((int)v);
		case LONG_TYPE:  return LongField.of((long)v);
		case BOOLEAN_TYPE: return BooleanField.of((int)v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}
	
	public IGenericField<?> fieldOf(double v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
		case INTEGER_TYPE: return IntegerField.of((int)v);
		case LONG_TYPE:  return LongField.of((long)v);
		case BOOLEAN_TYPE: return BooleanField.of((int)v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of((float)v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}
	
	public IGenericField<?> fieldOf(boolean v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
	//	case INTEGER_TYPE: return IntegerField.of(v);
	//	case LONG_TYPE:  return LongField.of((long)v);
		case BOOLEAN_TYPE: return BooleanField.of(v);
	//	case DOUBLE_TYPE: return DoubleField.of(v);
	//	case FLOAT_TYPE: return FloatField.of(v);
	//	case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
	}
	
	public IGenericField<?> fieldOf(short v) throws RowsTypeException {
		switch ( type ) {
		case STRING_TYPE: return from(String.valueOf(v));
		case INTEGER_TYPE: return IntegerField.of((int)v);
		case LONG_TYPE:  return LongField.of((long)v);
		case BOOLEAN_TYPE: return BooleanField.of(v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}
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
