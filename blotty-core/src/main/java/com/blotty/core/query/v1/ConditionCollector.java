/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.query.v1;

import java.util.ArrayList;
import java.util.List;

import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;

public class ConditionCollector {

	public List<IFilterCondition> collect( IFilterCondition root ){
		List<IFilterCondition> ret = new ArrayList<>();
		visit(root, ret);
		return ret;
	}
	
	private void visit(IFilterCondition node, List<IFilterCondition> result ) {
		if (node instanceof NotCondition || node instanceof OrCondition) {
			// NOT and OR subtrees are not indexable in V1
			return;
		}
		
		for (IFilterCondition child : node.children()) {
		    visit(child, result);
		}
		result.add(node);
	}
}
