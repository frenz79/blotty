package com.blotty.core.modifiers.filters.conditions;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.rows.Row;

public class NotCondition implements IFilterCondition {

    private final IFilterCondition cond;

    public NotCondition(
            IFilterCondition cond) {
        this.cond = cond;
    }

    @Override
    public boolean apply(Row row) throws FilterExpressionException {
        return !cond.apply(row);
    }
    public IFilterCondition getCondition() {
        return cond;
    }
}
