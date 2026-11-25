package com.blotty.core.modifiers.filters.conditions.unary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;
import com.blotty.core.rows.Row;
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

		Row rNull = Row.newBuilder(colModel).set(colModel.getKey(), "K1").setNull(col).build();
		Row rVal = Row.newBuilder(colModel).set(colModel.getKey(), "K2").set(col, "Value").build();

		assertTrue(isNull.apply(rNull));
		assertFalse(isNull.apply(rVal));
	}

	@Test
	void testIsNotNull() throws RowsTypeException {
		UnaryCondition isNotNull = new UnaryCondition(Operand.of(col), new IsNotNull());

		Row rNull = Row.newBuilder(colModel).set(colModel.getKey(), "K1").setNull(col).build();
		Row rVal = Row.newBuilder(colModel).set(colModel.getKey(), "K2").set(col, "Value").build();

		assertFalse(isNotNull.apply(rNull));
		assertTrue(isNotNull.apply(rVal));
	}
}
