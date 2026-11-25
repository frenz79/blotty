package com.blotty.core.modifiers.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
import com.blotty.core.models.rows.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;
import com.blotty.core.types.FieldType;

class FilterExpressionTest {

    @Test
    void testFilterOnKey()
            throws RowsTypeException,
                   ColumnsModelException,
                   FilterExpressionException {

        ColumnsModel colModel = ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("PK", true)
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("COL_1")
                        .type(FieldType.STRING_TYPE))
                .build();

        Column col1 = colModel.getColumn("COL_1");

        FilterExpression equal = new FilterExpression(
            new BinaryCondition(
                Operand.of(colModel.getKey()),
                new Equal(),
                Operand.of("KEY_2"))
        );

        Row row1 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_1")
                .set(col1, "TEST_VALUE_1")
                .build();

        Row row2 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_2")
                .set(col1, "TEST_VALUE_2")
                .build();

        assertFalse(equal.apply(row1));
        assertTrue(equal.apply(row2));
    }

    @Test
    void testEqual_strings()
            throws RowsTypeException,
                   ColumnsModelException,
                   FilterExpressionException {

        ColumnsModel colModel = ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("PK", true)
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("COL_1")
                        .type(FieldType.STRING_TYPE))
                .build();

        Column col1 = colModel.getColumn("COL_1");

        FilterExpression equal = new FilterExpression(
            new BinaryCondition(
                Operand.of(col1),
                new Equal(),
                Operand.of("TEST_VALUE_2"))
        );

        Row row1 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_1")
                .set(col1, "TEST_VALUE_1")
                .build();

        Row row2 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_2")
                .set(col1, "TEST_VALUE_2")
                .build();

        assertFalse(equal.apply(row1));
        assertTrue(equal.apply(row2));

        FilterExpression notEqual = new FilterExpression(
            new BinaryCondition(
                Operand.of(col1),
                new NotEqual(),
                Operand.of("TEST_VALUE_2"))
        );

        assertTrue(notEqual.apply(row1));
        assertFalse(notEqual.apply(row2));

        Row row3 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_3")
                .setNull(col1)
                .build();

        assertFalse(equal.apply(row3));
        assertFalse(equal.apply(row3));

        assertTrue(notEqual.apply(row3));
        assertTrue(notEqual.apply(row3));
    }

    @Test
    void testIsNull_strings()
            throws RowsTypeException,
                   ColumnsModelException,
                   FilterExpressionException {

        ColumnsModel colModel = ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("PK", true)
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("COL_1")
                        .type(FieldType.STRING_TYPE))
                .build();

        Column col1 = colModel.getColumn("COL_1");

        FilterExpression isNull = new FilterExpression(
            new UnaryCondition(
                Operand.of(col1),
                new IsNull())
        );

        FilterExpression isNotNull = new FilterExpression(
            new UnaryCondition(
                Operand.of(col1),
                new IsNotNull())
        );

        Row row1 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_1")
                .set(col1, "TEST_VALUE_1")
                .build();

        Row row2 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_2")
                .set(col1, "TEST_VALUE_2")
                .build();

        Row row3 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_3")
                .setNull(col1)
                .build();

        assertFalse(isNull.apply(row1));
        assertFalse(isNull.apply(row2));
        assertTrue(isNull.apply(row3));

        assertTrue(isNotNull.apply(row1));
        assertTrue(isNotNull.apply(row2));
        assertFalse(isNotNull.apply(row3));
    }

    @Test
    void testFilterConditionChain()
            throws ColumnsModelException,
                   FilterExpressionException,
                   RowsTypeException {

        ColumnsModel colModel = ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("PK", true)
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("COL_1")
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("COL_2")
                        .type(FieldType.STRING_TYPE))
                .build();

        Column col1 = colModel.getColumn("COL_1");
        Column col2 = colModel.getColumn("COL_2");

        Row row1 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_1")
                .set(col1, "AAA")
                .set(col2, "CCC")
                .build();

        Row row2 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_2")
                .set(col1, "YYY")
                .set(col2, "YYY")
                .build();

        Row row3 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_3")
                .setNull(col1)
                .set(col2, "ZZZ")
                .build();

        Row row4 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_4")
                .set(col1, "YYY")
                .set(col2, "ZZZ")
                .build();

        Row row5 = Row.newBuilder(colModel)
                .set(colModel.getKey(), "KEY_5")
                .set(col1, "XXX")
                .set(col2, "ZZZ")
                .build();

        IFilterCondition col1XXX =
            new BinaryCondition(
                Operand.of(col1),
                new Equal(),
                Operand.of("XXX"));

        IFilterCondition col1YYY =
            new BinaryCondition(
                Operand.of(col1),
                new Equal(),
                Operand.of("YYY"));

        IFilterCondition col2ZZZ =
            new BinaryCondition(
                Operand.of(col2),
                new Equal(),
                Operand.of("ZZZ"));

        // COL_1='XXX' AND COL_2='ZZZ'

        FilterExpression exprBuilder1 =
            FilterExpression.newBuilder()
                .begin(col1XXX)
                .and(col2ZZZ)
                .build();

        assertFalse(exprBuilder1.apply(row1));
        assertFalse(exprBuilder1.apply(row2));
        assertFalse(exprBuilder1.apply(row3));
        assertFalse(exprBuilder1.apply(row4));
        assertTrue(exprBuilder1.apply(row5));

        // COL_1='XXX' OR COL_2='ZZZ'

        FilterExpression exprBuilder2 =
            FilterExpression.newBuilder()
                .begin(col1XXX)
                .or(col2ZZZ)
                .build();

        assertFalse(exprBuilder2.apply(row1));
        assertFalse(exprBuilder2.apply(row2));
        assertTrue(exprBuilder2.apply(row3));
        assertTrue(exprBuilder2.apply(row4));
        assertTrue(exprBuilder2.apply(row5));

        /*
         * Builder chaining is evaluated in insertion order.
         *
         * begin(A)
         *   .or(B)
         *   .and(C)
         *
         * becomes:
         *
         *      (A OR B) AND C
         *
         * and NOT:
         *
         *      A OR (B AND C)
         */

        FilterExpression exprBuilder3 =
            FilterExpression.newBuilder()
                .begin(col1XXX)
                .or(col1YYY)
                .and(col2ZZZ)
                .build();

        assertFalse(exprBuilder3.apply(row1));
        assertFalse(exprBuilder3.apply(row2));
        assertFalse(exprBuilder3.apply(row3));
        assertTrue(exprBuilder3.apply(row4));
        assertTrue(exprBuilder3.apply(row5));
    }
}
