package com.blotty.core.modifiers.filters.conditions.binary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.NullField;

class BinaryConditionTest {

	private ColumnsModel colModel;
	private Column colA;
	private Column colB;
	private Column colText;

	@BeforeEach
	void setUp() throws ColumnsModelException {
		colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_A").type(FieldType.INTEGER_TYPE))
				.add(Column.newBuilder("COL_B").type(FieldType.INTEGER_TYPE))
				.add(Column.newBuilder("COL_TEXT").type(FieldType.STRING_TYPE))
				.build();
		colA = colModel.getColumn("COL_A");
		colB = colModel.getColumn("COL_B");
		colText = colModel.getColumn("COL_TEXT");
	}

	@Test
	void testColumnVsColumnComparison() throws RowsTypeException, FilterExpressionException {
		// Tests the fix for the ClassCastException when comparing two columns
		BinaryCondition condEqual = BinaryCondition.Equal(Operand.of(colA), Operand.of(colB));
		BinaryCondition condLess = BinaryCondition.LesserThan(Operand.of(colA), Operand.of(colB));
		BinaryCondition condGreater = BinaryCondition.GreaterThan(Operand.of(colA), Operand.of(colB));

		Row r1 = new Row(colModel).set(colModel.getKey(), "K1").set(colA, 10).set(colB, 10);
		Row r2 = new Row(colModel).set(colModel.getKey(), "K2").set(colA, 5).set(colB, 10);
		Row r3 = new Row(colModel).set(colModel.getKey(), "K3").set(colA, 15).set(colB, 10);

		assertTrue(condEqual.apply(r1));
		assertFalse(condEqual.apply(r2));
		assertFalse(condEqual.apply(r3));

		assertFalse(condLess.apply(r1));
		assertTrue(condLess.apply(r2));
		assertFalse(condLess.apply(r3));

		assertFalse(condGreater.apply(r1));
		assertFalse(condGreater.apply(r2));
		assertTrue(condGreater.apply(r3));
	}

	@Test
	void testColumnVsConstAndConstVsColumn() throws RowsTypeException, FilterExpressionException {
		BinaryCondition colVsConst = BinaryCondition.GreaterThanEqual(Operand.of(colA), Operand.of("10", colA));
		BinaryCondition constVsCol = BinaryCondition.LesserThanEqual(Operand.of("10", colA), Operand.of(colA));

		Row r1 = new Row(colModel).set(colModel.getKey(), "K1").set(colA, 10);
		Row r2 = new Row(colModel).set(colModel.getKey(), "K2").set(colA, 9);
		Row r3 = new Row(colModel).set(colModel.getKey(), "K3").set(colA, 11);

		assertTrue(colVsConst.apply(r1));
		assertFalse(colVsConst.apply(r2));
		assertTrue(colVsConst.apply(r3));

		assertTrue(constVsCol.apply(r1));
		assertFalse(constVsCol.apply(r2));
		assertTrue(constVsCol.apply(r3));
	}

	@Test
	void testLikeAndNotLikeWithNullHandling() throws RowsTypeException, FilterExpressionException {
		BinaryCondition like = BinaryCondition.Like(Operand.of(colText), Operand.of("World"));
		BinaryCondition notLike = BinaryCondition.NotLike(Operand.of(colText), Operand.of("World"));

		Row rMatch = new Row(colModel).set(colModel.getKey(), "K1").set(colText, "Hello World!");
		Row rNoMatch = new Row(colModel).set(colModel.getKey(), "K2").set(colText, "Hello Java!");
		Row rNull = new Row(colModel).set(colModel.getKey(), "K3").set(colText, NullField.NULL);

		assertTrue(like.apply(rMatch));
		assertFalse(like.apply(rNoMatch));
		assertFalse(like.apply(rNull));

		assertFalse(notLike.apply(rMatch));
		assertTrue(notLike.apply(rNoMatch));
		assertFalse(notLike.apply(rNull));
	}

	@Test
	void testAllComparisonOperators() throws RowsTypeException, FilterExpressionException {
		BinaryCondition eq = BinaryCondition.Equal(Operand.of(colA), Operand.of(10));
		BinaryCondition ne = BinaryCondition.NotEqual(Operand.of(colA), Operand.of(10));
		BinaryCondition gt = BinaryCondition.GreaterThan(Operand.of(colA), Operand.of(10));
		BinaryCondition gte = BinaryCondition.GreaterThanEqual(Operand.of(colA), Operand.of(10));
		BinaryCondition lt = BinaryCondition.LesserThan(Operand.of(colA), Operand.of(10));
		BinaryCondition lte = BinaryCondition.LesserThanEqual(Operand.of(colA), Operand.of(10));

		Row r10 = new Row(colModel).set(colModel.getKey(), "K1").set(colA, 10);
		Row r5 = new Row(colModel).set(colModel.getKey(), "K2").set(colA, 5);
		Row r15 = new Row(colModel).set(colModel.getKey(), "K3").set(colA, 15);

		assertTrue(eq.apply(r10));
		assertFalse(eq.apply(r5));

		assertFalse(ne.apply(r10));
		assertTrue(ne.apply(r5));

		assertTrue(gt.apply(r15));
		assertFalse(gt.apply(r10));

		assertTrue(gte.apply(r10));
		assertTrue(gte.apply(r15));
		assertFalse(gte.apply(r5));

		assertTrue(lt.apply(r5));
		assertFalse(lt.apply(r10));

		assertTrue(lte.apply(r10));
		assertTrue(lte.apply(r5));
		assertFalse(lte.apply(r15));
	}
}
