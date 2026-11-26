/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.types;

import com.blotty.core.types.impl.BooleanField;
import com.blotty.core.types.impl.DoubleField;
import com.blotty.core.types.impl.FloatField;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.LongField;
import com.blotty.core.types.impl.ShortField;
import com.blotty.core.types.impl.StringField;

public enum FieldType {
	
	STRING_TYPE(StringField.MIN_VALUE, StringField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return StringField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return StringField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return StringField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return StringField.of(v);	}
		@Override	public IGenericField<?> of(double v){return StringField.of(v);	}
		@Override	public IGenericField<?> of(String v){return StringField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return StringField.NULL;	}
	},	
	BOOLEAN_TYPE(BooleanField.FALSE, BooleanField.TRUE){
		@Override	public IGenericField<?> of(boolean v){return BooleanField.of(v);}
		@Override	public IGenericField<?> of(int    v){return BooleanField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return BooleanField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return BooleanField.of(v);	}
		@Override	public IGenericField<?> of(double v){return BooleanField.of(v);	}
		@Override	public IGenericField<?> of(String v){return BooleanField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return BooleanField.NULL;	}
	},	
	SHORT_TYPE(ShortField.MIN_VALUE, ShortField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return ShortField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return ShortField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return ShortField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return ShortField.of(v);	}
		@Override	public IGenericField<?> of(double v){return ShortField.of(v);	}
		@Override	public IGenericField<?> of(String v){return ShortField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return ShortField.NULL;	}
	},	
	INTEGER_TYPE(IntegerField.MIN_VALUE, IntegerField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> of(double v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> of(String v){return IntegerField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return IntegerField.NULL;	}
	},	
	LONG_TYPE(LongField.MIN_VALUE, LongField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return LongField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return LongField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return LongField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return LongField.of(v);	}
		@Override	public IGenericField<?> of(double v){return LongField.of(v);	}
		@Override	public IGenericField<?> of(String v){return LongField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return LongField.NULL;	}
	},	
	FLOAT_TYPE(FloatField.MIN_VALUE, FloatField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return FloatField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return FloatField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return FloatField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return FloatField.of(v);	}
		@Override	public IGenericField<?> of(double v){return FloatField.of(v);	}
		@Override	public IGenericField<?> of(String v){return FloatField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return FloatField.NULL;	}
	},
	DOUBLE_TYPE(DoubleField.MIN_VALUE, DoubleField.MAX_VALUE){
		@Override	public IGenericField<?> of(boolean v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> of(int    v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> of(long   v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> of(float  v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> of(double v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> of(String v){return DoubleField.of(v);	}
		@Override	public IGenericField<?> ofNull(    ){return DoubleField.NULL;	}
	};

	public abstract IGenericField<?> of(boolean v);
	public abstract IGenericField<?> of(int v);
	public abstract IGenericField<?> of(long v);
	public abstract IGenericField<?> of(float v);
	public abstract IGenericField<?> of(double v);
	public abstract IGenericField<?> of(String v);
	public abstract IGenericField<?> ofNull();
	
	private final IGenericField<?> min;
	private final IGenericField<?> max;
	
	public IGenericField<?> getDefaultMax() {
		return max;
	}

	public IGenericField<?> getDefaultMin() {
		return min;
	}

	private FieldType(IGenericField<?> min, IGenericField<?> max) {
		this.min = min;
		this.max = max;
	}	
}
