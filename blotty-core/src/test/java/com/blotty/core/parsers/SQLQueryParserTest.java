package com.blotty.core.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.StringField;

class SQLQueryParserTest {

	@Test
	void testSQLQuery() throws ColumnsModelException, FilterExpressionException, RowsTypeException {
		ColumnsModel colsModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_1").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_2").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("COL_3").type(FieldType.STRING_TYPE)
			).build();		
		
		FilterExpression parser = new SQLQueryParser().parse(
			colsModel, " \"COL_1\" = 'XXXX' AND \"COL_2\" > '40' OR \"COL_3\" < \"COL_2\" ");
		
		System.out.println("Query: "+parser);
		
		assertTrue( parser.getConditionsChain().getCondition() instanceof BinaryCondition );
		
		assertEquals("COL_1", ((BinaryCondition)parser.getConditionsChain().getCondition()).getLeft().getColumn().getName() );
		assertEquals(Equal.class.getSimpleName(), ((BinaryCondition)parser.getConditionsChain().getCondition()).getOperator().toString() );
		assertEquals(StringField.of("XXXX").toString(), ((BinaryCondition)parser.getConditionsChain().getCondition()).getRight().getOperand().toString() );
		
		assertEquals("COL_2", ((BinaryCondition)parser.getConditionsChain().getNextCondition().getCondition()).getLeft().getColumn().getName() );
		assertEquals(GreaterThan.class.getSimpleName(), ((BinaryCondition)parser.getConditionsChain().getNextCondition().getCondition()).getOperator().toString() );
		assertEquals(IntegerField.of(40).toString(), ((BinaryCondition)parser.getConditionsChain().getNextCondition().getCondition()).getRight().getOperand().toString() );

		assertEquals("COL_3", ((BinaryCondition)parser.getConditionsChain().getNextCondition().getNextCondition().getCondition()).getLeft().getColumn().getName() );
		assertEquals(LesserThan.class.getSimpleName(), ((BinaryCondition)parser.getConditionsChain().getNextCondition().getNextCondition().getCondition()).getOperator().toString() );
		assertEquals("COL_2", ((BinaryCondition)parser.getConditionsChain().getNextCondition().getNextCondition().getCondition()).getRight().getColumn().getName() );

	}

}
