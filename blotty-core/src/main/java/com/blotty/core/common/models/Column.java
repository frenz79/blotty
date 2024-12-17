package com.blotty.core.common.models;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.commons.GenericField;
import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.impl.BooleanField;
import com.blotty.core.common.models.types.impl.DoubleField;
import com.blotty.core.common.models.types.impl.FloatField;
import com.blotty.core.common.models.types.impl.IntegerField;
import com.blotty.core.common.models.types.impl.LongField;
import com.blotty.core.common.models.types.impl.NullField;
import com.blotty.core.common.models.types.impl.ShortField;
import com.blotty.core.common.models.types.impl.StringField;

public class Column {

	private int id = -1;
	private final String name;
	private final FieldType type;
	private final boolean cacheable;

	private final Map<String,Object> properties = new HashMap<>();
	private final Map<String,StringField> stringFieldsCache = new ConcurrentHashMap<>();

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

	private StringField from( String str ) {
		return (isCacheable())
				? stringFieldsCache.compute( str, (k, v) -> (v == null) ? StringField.of(str) : v)
				: StringField.of(str);
	}
	
	public GenericField fieldOf(String v) throws RowsTypeException {
		if (str==null || str.length()==0 ) {
			return NullField.NULL;
		}
		switch ( type ) {
		case NULL: 	return NullField.NULL;
		case STRING_TYPE: return from(v);
		case INTEGER_TYPE: return IntegerField.of((int)v);
		case LONG_TYPE:  return LongField.of(v);
		case BOOLEAN_TYPE: return BooleanField.of((int)v);
		case DOUBLE_TYPE: return DoubleField.of(v);
		case FLOAT_TYPE: return FloatField.of(v);
		case SHORT_TYPE: return ShortField.of((short)v);
		default: throw new RowsTypeException(String.format("Missing impl for type:%s", String.valueOf(type)));
		}		
	}
	
	public GenericField fieldOf(long v) throws RowsTypeException {
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
	
	public GenericField fieldOf(int v) throws RowsTypeException {
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
	

	public GenericField fieldOf(float v) throws RowsTypeException {
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
	
	public GenericField fieldOf(double v) throws RowsTypeException {
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
	
	public GenericField fieldOf(boolean v) throws RowsTypeException {
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
	
	public GenericField fieldOf(short v) throws RowsTypeException {
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
}
