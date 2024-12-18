package com.blotty.core.models;

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
import com.blotty.core.types.impl.NullField;
import com.blotty.core.types.impl.ShortField;
import com.blotty.core.types.impl.StringField;

public class Column {

	private int id = -1;
	private final String name;
	private final FieldType type;
	private final boolean cacheable;
	private final boolean key;

	private final Map<String,Object> properties = new HashMap<>();
	private final Map<String,StringField> stringFieldsCache = new ConcurrentHashMap<>();

	public Column( String n, FieldType type ){
		this.name = n;
		this.type = type;
		this.cacheable = false;
		this.key = false;
	}
	
	public Column( String n, boolean isKey, FieldType type ){
		this.name = n;
		this.type = type;
		this.cacheable = false;
		this.key = isKey;
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
		return key;
	}
}
