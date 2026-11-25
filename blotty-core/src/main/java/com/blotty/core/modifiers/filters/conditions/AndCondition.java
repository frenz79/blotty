/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters.conditions;

import java.util.List;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.rows.Row;

public class AndCondition implements IFilterCondition {

    private final IFilterCondition left;
    private final IFilterCondition right;

    public AndCondition(
            IFilterCondition left,
            IFilterCondition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean apply(Row row) throws FilterExpressionException {
        return left.apply(row)
            && right.apply(row);
    }

    @Override
    public List<IFilterCondition> children() {
        return List.of(left, right);
    }

    public IFilterCondition getLeft() {
        return left;
    }

    public IFilterCondition getRight() {
        return right;
    }
}
