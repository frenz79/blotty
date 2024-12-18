package com.blotty.core.modifiers.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.models.Row;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.NullField;

class FilterExpressionTest {

	@Test
	void testFilterOnKey() throws RowsTypeException, ColumnsModelException {		
		ColumnsModel colModel = new ColumnsModelBuilder()
			.addKey( "PK", FieldType.STRING_TYPE )
			.add( "COL_1", FieldType.STRING_TYPE )
			.build();
		
		Column col1 = colModel.getColumn("COL_1");
		
		FilterExpression equal = new FilterExpression(
			new BinaryCondition(Operand.of(colModel.getKey()), new Equal(), Operand.of("KEY_2"))
		);
		
		Row row1 = new Row(colModel).set(colModel.getKey(), "KEY_1").set(col1, "TEST_VALUE_1");
		Row row2 = new Row(colModel).set(colModel.getKey(), "KEY_2").set(col1, "TEST_VALUE_2");
		
		assertFalse( equal.apply( row1 ) );		
		assertTrue ( equal.apply( row2 ) );
	}
	
	@Test
	void testEqual_strings() throws RowsTypeException, ColumnsModelException {		
		ColumnsModel colModel = new ColumnsModelBuilder()
			.addKey( "PK", FieldType.STRING_TYPE )
			.add( "COL_1", FieldType.STRING_TYPE )
			.build();
		
		Column col1 = colModel.getColumn("COL_1");
		
		FilterExpression equal = new FilterExpression(
			new BinaryCondition(Operand.of(col1), new Equal(), Operand.of("TEST_VALUE_2"))
		);
		
		Row row1 = new Row(colModel).set(colModel.getKey(), "KEY_1").set(col1, "TEST_VALUE_1");
		Row row2 = new Row(colModel).set(colModel.getKey(), "KEY_2").set(col1, "TEST_VALUE_2");
		
		assertFalse( equal.apply( row1 ) );		
		assertTrue ( equal.apply( row2 ) );
		
		FilterExpression notEqual = new FilterExpression(
			new BinaryCondition(Operand.of(col1), new NotEqual(), Operand.of("TEST_VALUE_2"))
		);
		
		assertTrue ( notEqual.apply( row1 ) );		
		assertFalse( notEqual.apply( row2 ) );
				
		Row row3 = new Row(colModel).set(colModel.getKey(), "KEY_3").set(col1, NullField.NULL);
		
		assertFalse( equal.apply( row3 ) );		
		assertFalse( equal.apply( row3 ) );
		assertTrue ( notEqual.apply( row3 ) );		
		assertTrue ( notEqual.apply( row3 ) );
	}
	
	@Test
	void testIsNull_strings() throws RowsTypeException, ColumnsModelException {		
		ColumnsModel colModel = new ColumnsModelBuilder()
			.addKey( "PK", FieldType.STRING_TYPE )
			.add( "COL_1", FieldType.STRING_TYPE )
			.build();
		
		Column col1 = colModel.getColumn("COL_1");
		
		FilterExpression isNull = new FilterExpression(
			new UnaryCondition(Operand.of(col1), new IsNull())
		);
		
		FilterExpression isNotNull = new FilterExpression(
			new UnaryCondition(Operand.of(col1), new IsNotNull())
		);
		
		Row row1 = new Row(colModel).set(colModel.getKey(), "KEY_1").set(col1, "TEST_VALUE_1");
		Row row2 = new Row(colModel).set(colModel.getKey(), "KEY_2").set(col1, "TEST_VALUE_2");
		Row row3 = new Row(colModel).set(colModel.getKey(), "KEY_3").set(col1, NullField.NULL);
		
		assertFalse( isNull.apply( row1 ) );		
		assertFalse( isNull.apply( row2 ) );
		assertTrue ( isNull.apply( row3 ) );
		
		assertTrue ( isNotNull.apply( row1 ) );		
		assertTrue ( isNotNull.apply( row2 ) );
		assertFalse( isNotNull.apply( row3 ) );
	}
	
	@Test
	void testFilterConditionChain() throws ColumnsModelException, FilterExpressionException, RowsTypeException {
		ColumnsModel colModel = new ColumnsModelBuilder()
				.addKey( "PK", FieldType.STRING_TYPE )
				.add( "COL_1", FieldType.STRING_TYPE )
				.add( "COL_2", FieldType.STRING_TYPE )
				.build();
			
		Column col1 = colModel.getColumn("COL_1");
		Column col2 = colModel.getColumn("COL_2");
		
		Row row1 = new Row(colModel).set(colModel.getKey(), "KEY_1").set(col1, "AAA").set(col2, "CCC");
		Row row2 = new Row(colModel).set(colModel.getKey(), "KEY_2").set(col1, "YYY").set(col2, "YYY");
		Row row3 = new Row(colModel).set(colModel.getKey(), "KEY_3").set(col1, NullField.NULL).set(col2, "ZZZ");
		Row row4 = new Row(colModel).set(colModel.getKey(), "KEY_4").set(col1, "YYY").set(col2, "ZZZ");
		Row row5 = new Row(colModel).set(colModel.getKey(), "KEY_5").set(col1, "XXX").set(col2, "ZZZ");
		
		FilterExpression exprBuilder1 = new FilterExpressionBuilder()
			.begin(	new BinaryCondition(Operand.of(col1), new Equal(), Operand.of("XXX")))
			.and( new BinaryCondition(Operand.of(col2), new Equal(), Operand.of("ZZZ")))
		.build();
		
		assertFalse( exprBuilder1.apply( row1 ) );
		assertFalse( exprBuilder1.apply( row2 ) );
		assertFalse( exprBuilder1.apply( row3 ) );
		assertFalse( exprBuilder1.apply( row4 ) );	
		assertTrue ( exprBuilder1.apply( row5 ) );	
		
		FilterExpression exprBuilder2 = new FilterExpressionBuilder()
			.begin(	new BinaryCondition(Operand.of(col1), new Equal(), Operand.of("XXX")))
			.or( new BinaryCondition(Operand.of(col2), new Equal(), Operand.of("ZZZ")))
		.build();
		
		assertFalse( exprBuilder2.apply( row1 ) );
		assertFalse( exprBuilder2.apply( row2 ) );
		assertTrue ( exprBuilder2.apply( row3 ) );
		assertTrue ( exprBuilder2.apply( row4 ) );	
		assertTrue ( exprBuilder2.apply( row5 ) );
		
		FilterExpression exprBuilder3 = new FilterExpressionBuilder()
			.begin(	new BinaryCondition(Operand.of(col1), new Equal(), Operand.of("XXX")))
			.or ( new BinaryCondition(Operand.of(col1), new Equal(), Operand.of("YYY")))
			.and( new BinaryCondition(Operand.of(col2), new Equal(), Operand.of("ZZZ")))
		.build();
		
		assertFalse( exprBuilder3.apply( row1 ) );
		assertFalse( exprBuilder3.apply( row2 ) );
		assertFalse( exprBuilder3.apply( row3 ) );
		assertTrue ( exprBuilder3.apply( row4 ) );		
	}
}
