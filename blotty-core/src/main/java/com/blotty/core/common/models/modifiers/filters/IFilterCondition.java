package com.blotty.core.common.models.modifiers.filters;

import com.blotty.core.common.models.Row;

public interface IFilterCondition {

	public boolean apply( Row row );
	
}
