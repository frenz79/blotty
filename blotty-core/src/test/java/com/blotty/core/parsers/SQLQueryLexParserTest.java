package com.blotty.core.parsers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.logical.AndCondition;
import com.blotty.core.modifiers.filters.conditions.logical.NotCondition;
import com.blotty.core.modifiers.filters.conditions.logical.OrCondition;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.types.FieldType;

class SQLQueryLexParserTest {

    private SQLQueryLexParser parser;
    private ColumnsModel columns;

    @BeforeEach
    void setup() throws Exception {

        columns =
            ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("ID", true)
                        .type(FieldType.INTEGER_TYPE))
                .add(
                    Column.newBuilder("NAME")
                        .type(FieldType.STRING_TYPE))
                .add(
                    Column.newBuilder("AGE")
                        .type(FieldType.INTEGER_TYPE))
                .add(
                    Column.newBuilder("ACTIVE")
                        .type(FieldType.BOOLEAN_TYPE))
                .build();

        parser = new SQLQueryLexParser();
    }

    @Test
    void shouldParseEqual() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"NAME\" = 'PIPPO'");

        assertNotNull(expr);
        assertInstanceOf(
            BinaryCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseGreaterThan() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"AGE\" > 18");

        assertNotNull(expr);
        assertInstanceOf(
            BinaryCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseLike() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"NAME\" LIKE 'PIP'");

        assertInstanceOf(
            BinaryCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseIsNull() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"NAME\" IS NULL");

        assertInstanceOf(
            UnaryCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseIsNotNull() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"NAME\" IS NOT NULL");

        assertInstanceOf(
            UnaryCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseNot() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "NOT \"ACTIVE\" = true");

        assertInstanceOf(
            NotCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseAnd() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"AGE\" > 18 AND \"ACTIVE\" = true");

        assertInstanceOf(
            AndCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldParseOr() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"AGE\" > 18 OR \"ACTIVE\" = true");

        assertInstanceOf(
            OrCondition.class,
            expr.getRoot());
    }

    @Test
    void shouldApplyAndBeforeOr() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"AGE\" > 18 "
                    + "OR "
                    + "\"ACTIVE\" = true "
                    + "AND "
                    + "\"NAME\" = 'PIPPO'");

        assertInstanceOf(
            OrCondition.class,
            expr.getRoot());

        OrCondition root =
            (OrCondition) expr.getRoot();

        assertInstanceOf(
            AndCondition.class,
            root.getRight());
    }

    @Test
    void shouldRespectParenthesis() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "(\"AGE\" > 18 OR \"ACTIVE\" = true)"
                    + " AND "
                    + "\"NAME\" = 'PIPPO'");

        assertInstanceOf(
            AndCondition.class,
            expr.getRoot());

        AndCondition root =
            (AndCondition) expr.getRoot();

        assertInstanceOf(
            OrCondition.class,
            root.getLeft());
    }

    @Test
    void shouldEvaluateSimpleExpression() throws Exception {

        Row row =
            Row.newBuilder(columns)
                .set("ID", "1")
                .set("NAME", "PIPPO")
                .set("AGE", "50")
                .set("ACTIVE", "true")
                .build();

        FilterExpression expr =
            parser.parse(
                columns,
                "\"NAME\"='PIPPO' AND \"AGE\">18");

        assertTrue(expr.apply(row));
    }

    @Test
    void shouldEvaluateComplexExpression() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "(\"NAME\"='PIPPO' OR \"NAME\"='PLUTO') "
                    + "AND "
                    + "\"AGE\">18 "
                    + "AND "
                    + "NOT \"ACTIVE\" = false");

        Row matchingRow =
            Row.newBuilder(columns)
                .set("ID", "1")
                .set("NAME", "PIPPO")
                .set("AGE", "50")
                .set("ACTIVE", "true")
                .build();

        Row nonMatchingRow =
            Row.newBuilder(columns)
                .set("ID", "2")
                .set("NAME", "TOPOLINO")
                .set("AGE", "50")
                .set("ACTIVE", "true")
                .build();

        assertTrue(expr.apply(matchingRow));
        assertFalse(expr.apply(nonMatchingRow));
    }

    @Test
    void shouldSupportNegativeNumbers() throws Exception {

        FilterExpression expr =
            parser.parse(
                columns,
                "\"AGE\" > -10");

        Row row =
            Row.newBuilder(columns)
                .set("ID", "1")
                .set("NAME", "PIPPO")
                .set("AGE", "50")
                .set("ACTIVE", "true")
                .build();

        assertTrue(expr.apply(row));
    }

    @Test
    void shouldSupportColumnToColumnComparison() throws Exception {

        ColumnsModel cols =
            ColumnsModel.newBuilder()
                .add(
                    Column.newBuilder("ID", true)
                        .type(FieldType.INTEGER_TYPE))
                .add(
                    Column.newBuilder("FROM_QTY")
                        .type(FieldType.INTEGER_TYPE))
                .add(
                    Column.newBuilder("TO_QTY")
                        .type(FieldType.INTEGER_TYPE))
                .build();

        FilterExpression expr =
            parser.parse(
                cols,
                "\"FROM_QTY\" < \"TO_QTY\"");

        Row row =
            Row.newBuilder(cols)
                .set("ID", "1")
                .set("FROM_QTY", "10")
                .set("TO_QTY", "20")
                .build();

        assertTrue(expr.apply(row));
    }

    @Test
    void shouldFailOnUnknownColumn() {

        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(
                columns,
                "\"UNKNOWN\"='ABC'"));
    }

    @Test
    void shouldFailOnMissingSingleQuote() {

        assertThrows(
            FilterExpressionException.class,
            () -> parser.parse(
                columns,
                "\"NAME\"='PIPPO"));
    }

    @Test
    void shouldFailOnMissingClosingParenthesis() {

        assertThrows(
            FilterExpressionException.class,
            () -> parser.parse(
                columns,
                "(\"NAME\"='PIPPO'"));
    }

    @Test
    void shouldFailOnInvalidCondition() {

        assertThrows(
            FilterExpressionException.class,
            () -> parser.parse(
                columns,
                "\"NAME\" AND \"AGE\""));
    }

    @Test
    void shouldFailOnNotWithoutExpression() {

        assertThrows(
            FilterExpressionException.class,
            () -> parser.parse(
                columns,
                "NOT"));
    }
}
