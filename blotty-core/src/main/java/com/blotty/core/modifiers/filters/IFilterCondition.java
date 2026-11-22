package com.blotty.core.modifiers.filters;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Row;

public interface IFilterCondition {

	public boolean apply( Row row ) throws FilterExpressionException;
	
}
