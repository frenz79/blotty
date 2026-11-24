/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters;

import java.awt.IllegalComponentStateException;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.rows.Row;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;

public class FilterExpression {
	
	private IFilterCondition root;
	
	public FilterExpression( IFilterCondition root ) {
		this.root = root;
	}
	
	public boolean apply(Row row) throws FilterExpressionException {
    	return root.apply(row);
	}
	
	@Override
	public String toString() {
		return "FilterExpression [root=" + root + "]";
	}

	public IFilterCondition getRoot() {
		return root;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	public static class Builder {
		private IFilterCondition root;
		private IFilterCondition currentAndGroup;
		
		public Builder begin( IFilterCondition c ) {
			this.root = c;
			this.currentAndGroup = c;
			return this;
		}
		
		public Builder beginNot( IFilterCondition c ) {
			return begin(new NotCondition(c));
		}
		
		public Builder and( IFilterCondition c ) {
			checkInit();
			this.currentAndGroup = new AndCondition(currentAndGroup, c);
			replaceCurrentTerm(currentAndGroup);
			return this;
		}
		
		public Builder andNot( IFilterCondition c ) {
			return and(new NotCondition(c));
		}
		
		public Builder or( IFilterCondition c ) {
			checkInit();
			this.currentAndGroup = new OrCondition(currentAndGroup, c);
			replaceCurrentTerm(currentAndGroup);
			return this;
		}
		
		public Builder orNot( IFilterCondition c ) {
			return or(new NotCondition(c));
		}
		
		public FilterExpression build( ) {
			checkInit();
			return new FilterExpression(root);
		}
			
		private void checkInit() {
			if (root==null) 
				throw new IllegalComponentStateException("begin() must be called first");
		}
		
		private void replaceCurrentTerm( IFilterCondition c) {
			if (root==currentAndGroup) {
				root = c;
				return;
			}
			root = replaceRightMostTerm(root, c);
		}
		
		private IFilterCondition replaceRightMostTerm( IFilterCondition node, IFilterCondition repl ) {
			if ( !(node instanceof OrCondition)) {
				return repl;
			}
			OrCondition or = (OrCondition)node;
			return new OrCondition(
				or.getLeft(),
				replaceRightMostTerm(
					or.getRight(),
					repl
				)
			);
		}
	}
} 
