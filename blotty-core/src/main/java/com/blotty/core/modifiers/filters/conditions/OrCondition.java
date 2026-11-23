package com.blotty.core.modifiers.filters.conditions;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Row;

public class OrCondition implements IFilterCondition {

    private final IFilterCondition left;
    private final IFilterCondition right;

    public OrCondition(
            IFilterCondition left,
            IFilterCondition right) {

        this.left = left;
        this.right = right;
    }

    @Override
    public boolean apply(Row row) throws FilterExpressionException {
        return left.apply(row)
            || right.apply(row);
    }

    public IFilterCondition getLeft() {
        return left;
    }

    public IFilterCondition getRight() {
        return right;
    }
}
