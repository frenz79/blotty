package com.blotty.core.modifiers.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.types.FieldType;

class FilterExpressionVarargsTest {

	private ColumnsModel colModel;
	private Column c1;
	private Column c2;
	private Column c3;

	@BeforeEach
	void setUp() throws ColumnsModelException {
		colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("C1").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("C2").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("C3").type(FieldType.STRING_TYPE))
				.build();
		c1 = colModel.getColumn("C1");
		c2 = colModel.getColumn("C2");
		c3 = colModel.getColumn("C3");
	}

	@Test
	void testVarargsConstructorChaining() {
		// Tests the fix where varargs constructor previously lost all conditions after the first
		IFilterCondition cond1 = BinaryCondition.Equal(Operand.of(c1), Operand.of("A"));
		IFilterCondition cond2 = BinaryCondition.Equal(Operand.of(c2), Operand.of("B"));
		IFilterCondition cond3 = BinaryCondition.Equal(Operand.of(c3), Operand.of("C"));

		FilterExpression expr = FilterExpression.newBuilder().begin(cond1).and(cond2).and(cond3).build();

		assertNotNull(expr.getRoot());
		assertEquals(cond1, expr.getRoot().children().get(0));

		assertNotNull(expr.getRoot());
		assertEquals(cond1, expr.getRoot().children().get(1));

		assertNotNull(expr.getRoot());
		assertEquals(cond1, expr.getRoot().children().get(2));
	}

	@Test
	void testVarargsApply() throws RowsTypeException, FilterExpressionException {
		IFilterCondition cond1 = BinaryCondition.Equal(Operand.of(c1), Operand.of("A"));
		IFilterCondition cond2 = BinaryCondition.Equal(Operand.of(c2), Operand.of("B"));
		FilterExpression expr = FilterExpression.newBuilder().begin(cond1).and(cond2).build();

		Row match = new Row(colModel).set(colModel.getKey(), "K1").set(c1, "A").set(c2, "B").set(c3, "C");
		Row mismatch1 = new Row(colModel).set(colModel.getKey(), "K2").set(c1, "X").set(c2, "B").set(c3, "C");
		Row mismatch2 = new Row(colModel).set(colModel.getKey(), "K3").set(c1, "A").set(c2, "X").set(c3, "C");

		assertTrue(expr.apply(match));
		assertFalse(expr.apply(mismatch1));
		assertFalse(expr.apply(mismatch2));
	}
}
