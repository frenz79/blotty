package com.blotty.core.modifiers.filters.conditions;

import java.util.List;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Row;

public interface IFilterCondition {

	public boolean apply( Row row ) throws FilterExpressionException;

	default List<IFilterCondition> children() { return List.of(); }
}
