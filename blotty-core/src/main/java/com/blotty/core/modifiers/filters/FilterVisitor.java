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
}
