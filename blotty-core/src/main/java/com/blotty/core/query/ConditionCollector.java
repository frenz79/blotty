/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.query;

import java.util.ArrayList;
import java.util.List;

import com.blotty.core.modifiers.filters.conditions.AndCondition;
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
		/*
		if (node instanceof AndCondition and) {
			visit(and.getLeft(), result);
			visit(and.getRight(), result);
			return;
		}
		if (node instanceof OrCondition or) {
			visit(or.getLeft(), result);
			visit(or.getRight(), result);
			return;
		}
		*/
		if (node instanceof NotCondition) {
			// visit(not.getCondition(), result);
			// NOT subtrees are not indexable:
			// Avoid NOT STATUS='OPEN' to use HashIndex(STATUS)='OPEN'
			return;
		}
		
		for (IFilterCondition child : node.children()) {
		    visit(child, result);
		}
		result.add(node);
	}
}
