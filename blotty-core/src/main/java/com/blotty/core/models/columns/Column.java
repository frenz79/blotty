package com.blotty.core.models.columns;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.indexes.AbstractIndex;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.IGenericField;
import com.blotty.core.types.impl.BooleanField;
import com.blotty.core.types.impl.DoubleField;
import com.blotty.core.types.impl.FloatField;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.LongField;
import com.blotty.core.types.impl.NullField;
import com.blotty.core.types.impl.ShortField;
import com.blotty.core.types.impl.StringField;

public final class Column {
	
	private int id = -1;
	private final String name;
	private FieldType type;
	private boolean cacheable;
	private final boolean isKey;
	
	private Map<String,Object> properties;
	private final Map<String,StringField> stringFieldsCache = new ConcurrentHashMap<>();
	
	private AbstractIndex searchIndex;
	private final ColumnStatistics statistics;
	
	public Column( String n, boolean isKey ){
		this.name = n;
		this.isKey = isKey;
		this.statistics = new ColumnStatistics();
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
	        ? stringFieldsCache.computeIfAbsent(str, StringField::of)
	        : StringField.of(str);
	}
	
	public IGenericField fieldOf(String v) throws RowsTypeException {
		if (v==null || v.length()==0 ) {
			return NullField.NULL;
		}
		switch ( type ) {
		case NULL: 	return NullField.NULL;
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
	
	public IGenericField fieldOf(long v) throws RowsTypeException {
		switch ( type ) {
		case NULL: 	return NullField.NULL;
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
	
	public IGenericField fieldOf(int v) throws RowsTypeException {
		switch ( type ) {
		case NULL: return NullField.NULL;
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
	

	public IGenericField fieldOf(float v) throws RowsTypeException {
		switch ( type ) {
		case NULL: return NullField.NULL;
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
	
	public IGenericField fieldOf(double v) throws RowsTypeException {
		switch ( type ) {
		case NULL: return NullField.NULL;
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
	
	public IGenericField fieldOf(boolean v) throws RowsTypeException {
		switch ( type ) {
		case NULL: return NullField.NULL;
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
	
	public IGenericField fieldOf(short v) throws RowsTypeException {
		switch ( type ) {
		case NULL: return NullField.NULL;
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

	public Map<String, StringField> getStringFieldsCache() {
		return stringFieldsCache;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
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
		
		public Builder type(FieldType t) {
			this.col.setType(t);
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

	public ColumnStatistics getStatistics() {
		return statistics;
	}
}
