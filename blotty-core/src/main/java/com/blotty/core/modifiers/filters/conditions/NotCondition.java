/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
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
