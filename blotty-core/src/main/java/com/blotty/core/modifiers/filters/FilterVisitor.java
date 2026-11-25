/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.modifiers.filters;

import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;

interface FilterVisitor<T> {

    T visit(BinaryCondition c);

    T visit(UnaryCondition c);

    T visit(AndCondition c);

    T visit(OrCondition c);

    T visit(NotCondition c);
}
