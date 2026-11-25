/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.rows.Row;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;

/**
 * Represents a filter expression composed by an Abstract Syntax Tree (AST).
 *
 * The AST is built from IFilterCondition nodes such as:
 *
 * - BinaryCondition
 * - UnaryCondition
 * - AndCondition
 * - OrCondition
 * - NotCondition
 *
 * Filter expressions can be created directly by instantiating condition
 * objects or through the embedded Builder.
 *
 * Example:
 *
 *      COUNTRY='IT'
 *      AND
 *      EMPLOYEES > 100
 *
 * becomes:
 *
 *              AND
 *             /   \
 *            EQ    GT
 *
 * The resulting tree can later be evaluated against a Row through
 * the apply(...) method.
 */
public class FilterExpression {
	
	private IFilterCondition root;
	
	public FilterExpression( IFilterCondition root ) {
		this.root = root;
	}

	/**
	 * Evaluates the filter expression against the specified row.
	 *
	 * @param row row to evaluate
	 * @return true if the row satisfies the expression
	 */
	public boolean apply(Row row) throws FilterExpressionException {
    	return root.apply(row);
	}
	
	@Override
	public String toString() {
		return "FilterExpression [root=" + root + "]";
	}
	/**
	 * Returns the root node of the expression AST.
	 */
	public IFilterCondition getRoot() {
		return root;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Fluent builder for constructing filter ASTs.
	 *
	 * Unlike SQL parsing, the builder does NOT apply any operator
	 * precedence rules.
	 *
	 * Conditions are chained exactly in the order they are added.
	 *
	 * Example:
	 *
	 *      begin(A)
	 *      .or(B)
	 *      .and(C)
	 *
	 * generates:
	 *
	 *          AND
	 *         /   \
	 *       OR     C
	 *      /  \
	 *     A    B
	 *
	 * equivalent to:
	 *
	 *      (A OR B) AND C
	 *
	 * If a different grouping is required, nested conditions should
	 * be built separately and then attached as a single node.
	 */
	public static class Builder {
	/*
	 * NOTE:
	 *
	 * The SQLQueryParser and the Builder behave differently.
	 *
	 * SQLQueryParser:
	 *
	 *      A OR B AND C
	 *
	 * is interpreted as:
	 *
	 *      A OR (B AND C)
	 *
	 * according to SQL operator precedence.
	 *
	 * Builder:
	 *
	 *      begin(A)
	 *      .or(B)
	 *      .and(C)
	 *
	 * is interpreted as:
	 *
	 *      (A OR B) AND C
	 *
	 * because conditions are composed strictly in the order
	 * they are added.
	 */
	    private IFilterCondition root;

		/**
		 * Defines the root condition of the expression.
		 *
		 * This method must be called before any other builder operation.
		 */
	    public Builder begin(IFilterCondition condition) {
	        this.root = condition;
	        return this;
	    }
		/**
		 * Defines a NOT condition as the root of the expression.
		 *
		 * Example:
		 *
		 *      NOT STATUS='OPEN'
		 */
		public Builder beginNot(IFilterCondition condition) {
		    return begin( new NotCondition(condition));
		}	
		/**
		 * Appends a new AND node.
		 *
		 * Example:
		 *
		 *      begin(A)
		 *      .and(B)
		 *
		 * becomes:
		 *
		 *          AND
		 *         /   \
		 *        A     B
		 */
	    public Builder and(IFilterCondition condition) {
	        checkInit();
	        root = new AndCondition(root,condition);
	        return this;
	    }
		/**
		 * Appends a new OR node.
		 *
		 * Example:
		 *
		 *      begin(A)
		 *      .or(B)
		 *
		 * becomes:
		 *
		 *          OR
		 *         /  \
		 *        A    B
		 */
	    public Builder or(IFilterCondition condition) {
	        checkInit();
	        root = new OrCondition(root,condition);
	        return this;
	    }
		/**
		 * Shortcut for:
		 *
		 *      and(new NotCondition(condition))
		 */
	    public Builder andNot(IFilterCondition condition) {
	        return and(new NotCondition(condition));
	    }
		/**
		 * Shortcut for:
		 *
		 *      or(new NotCondition(condition))
		 */
	    public Builder orNot(IFilterCondition condition) {
	        return or(new NotCondition(condition));
	    }
		/**
		 * Creates the FilterExpression wrapping the generated AST.
		 */
	    public FilterExpression build() {
	        checkInit();
	        return new FilterExpression(root);
	    }
	
	    private void checkInit() {
	        if (root == null) {
	            throw new IllegalStateException( "begin() must be called first");
	        }
	    }
	}
} 
