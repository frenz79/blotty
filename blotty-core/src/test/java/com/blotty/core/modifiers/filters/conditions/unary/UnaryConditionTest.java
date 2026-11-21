package com.blotty.core.modifiers.filters.conditions.unary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.NullField;

class UnaryConditionTest {

	private ColumnsModel colModel;
	private Column col;

	@BeforeEach
	void setUp() throws ColumnsModelException {
		colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_1").type(FieldType.STRING_TYPE))
				.build();
		col = colModel.getColumn("COL_1");
	}

	@Test
	void testIsNull() throws RowsTypeException {
		UnaryCondition isNull = new UnaryCondition(Operand.of(col), new IsNull());

		Row rNull = new Row(colModel).set(colModel.getKey(), "K1").set(col, NullField.NULL);
		Row rVal = new Row(colModel).set(colModel.getKey(), "K2").set(col, "Value");

		assertTrue(isNull.apply(rNull));
		assertFalse(isNull.apply(rVal));
	}

	@Test
	void testIsNotNull() throws RowsTypeException {
		UnaryCondition isNotNull = new UnaryCondition(Operand.of(col), new IsNotNull());

		Row rNull = new Row(colModel).set(colModel.getKey(), "K1").set(col, NullField.NULL);
		Row rVal = new Row(colModel).set(colModel.getKey(), "K2").set(col, "Value");

		assertFalse(isNotNull.apply(rNull));
		assertTrue(isNotNull.apply(rVal));
	}
}
