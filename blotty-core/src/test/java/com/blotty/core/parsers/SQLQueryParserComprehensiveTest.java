package com.blotty.core.parsers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.NullField;

class SQLQueryParserComprehensiveTest {

	private ColumnsModel colModel;
	private Column col1;
	private Column col2;
	private Column colNum;

	@BeforeEach
	void setUp() throws ColumnsModelException {
		colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_1").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_2").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("NUM").type(FieldType.INTEGER_TYPE)
			).build();
		
		col1 = colModel.getColumn("COL_1");
		col2 = colModel.getColumn("COL_2");
		colNum = colModel.getColumn("NUM");
	}

	@Test
	void testSingleConditionQuery() throws Exception {
		// Previously crashed with NullPointerException when only 1 condition was parsed
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" = 'HELLO' ");
		assertNotNull(expr);

		Row rMatch = new Row(colModel).set(colModel.getKey(), "K1").set(col1, "HELLO");
		Row rNoMatch = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "BYE");

		assertTrue(expr.apply(rMatch));
		assertFalse(expr.apply(rNoMatch));
	}

	@Test
	void testIsNullQuery() throws Exception {
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" IS NULL ");

		Row rNull = new Row(colModel).set(colModel.getKey(), "K1").set(col1, NullField.NULL);
		Row rNonNull = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "NOT_NULL");

		assertTrue(expr.apply(rNull));
		assertFalse(expr.apply(rNonNull));
	}

	@Test
	void testIsNotNullQuery() throws Exception {
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" IS NOT NULL ");

		Row rNull = new Row(colModel).set(colModel.getKey(), "K1").set(col1, NullField.NULL);
		Row rNonNull = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "NOT_NULL");

		assertFalse(expr.apply(rNull));
		assertTrue(expr.apply(rNonNull));
	}

	@Test
	void testLikeQuery() throws Exception {
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" LIKE 'World' ");

		Row rMatch = new Row(colModel).set(colModel.getKey(), "K1").set(col1, "Hello World");
		Row rNoMatch = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "Hello Java");

		assertTrue(expr.apply(rMatch));
		assertFalse(expr.apply(rNoMatch));
	}

	@Test
	void testColumnVsColumnQuery() throws Exception {
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" = \"COL_2\" ");

		Row rEqual = new Row(colModel).set(colModel.getKey(), "K1").set(col1, "SAME").set(col2, "SAME");
		Row rDiff = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "A").set(col2, "B");

		assertTrue(expr.apply(rEqual));
		assertFalse(expr.apply(rDiff));
	}

	@Test
	void testEmptyQueryThrowsException() {
		SQLQueryParser parser = new SQLQueryParser();
		assertThrows(FilterExpressionException.class, () -> parser.parse(colModel, "   "));
		assertThrows(FilterExpressionException.class, () -> parser.parse(colModel, ""));
	}

	@Test
	void testCombinedAndOrQuery() throws Exception {
		SQLQueryParser parser = new SQLQueryParser();
		FilterExpression expr = parser.parse(colModel, " \"COL_1\" = 'A' AND \"NUM\" >= '10' OR \"COL_2\" = 'SPECIAL' ");

		Row r1 = new Row(colModel).set(colModel.getKey(), "K1").set(col1, "A").set(colNum, 10).set(col2, "X");
		Row r2 = new Row(colModel).set(colModel.getKey(), "K2").set(col1, "A").set(colNum, 5).set(col2, "X");
		Row r3 = new Row(colModel).set(colModel.getKey(), "K3").set(col1, "B").set(colNum, 2).set(col2, "SPECIAL");

		assertTrue(expr.apply(r1));
		assertFalse(expr.apply(r2));
		assertTrue(expr.apply(r3));
	}
}
