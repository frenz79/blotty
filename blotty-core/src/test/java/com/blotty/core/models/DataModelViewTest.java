package com.blotty.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.IntegerField;

class DataModelViewTest {

	@Test
	void testDynamicRowAdditionWithSorting() throws Exception {
		ColumnsModel colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("VAL").type(FieldType.INTEGER_TYPE)
			).build();
	
		DataModel dataModel = new DataModel(new InMemoryRowsModel(colModel));
		dataModel.addRow( Row.newBuilder(colModel).setValues("K1", "30") );
		dataModel.addRow( Row.newBuilder(colModel).setValues("K2", "10") );

		// View sorted AscNullLast on VAL
		DataModelView view = dataModel.createView("V1",
				new QueryDefinition(null,
				new SorterExpressionBuilder(colModel).AscNullLast("VAL").build()));

		assertEquals(2, view.getRowsCount());
		assertEquals(IntegerField.of(10), view.getRow(0).getFields()[colModel.getColumn("VAL").getId()]);
		assertEquals(IntegerField.of(30), view.getRow(1).getFields()[colModel.getColumn("VAL").getId()]);

		// Add a new row to parent model that should be sorted in between (VAL = 20)
		dataModel.addRow( Row.newBuilder(colModel).setValues("K3", "20") );
	
		// View should automatically receive K3 and re-sort dynamically
		assertEquals(3, view.getRowsCount());
		assertEquals(IntegerField.of(10), view.getRow(0).getFields()[colModel.getColumn("VAL").getId()]);
		assertEquals(IntegerField.of(20), view.getRow(1).getFields()[colModel.getColumn("VAL").getId()]);
		assertEquals(IntegerField.of(30), view.getRow(2).getFields()[colModel.getColumn("VAL").getId()]);
	}

	@Test
	void testViewFilterAndSortTogether() throws Exception {
		ColumnsModel colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("NAME").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("SCORE").type(FieldType.INTEGER_TYPE)
			).build();

		DataModel dataModel = new DataModel(new InMemoryRowsModel(colModel));
		dataModel.addRow( Row.newBuilder(colModel).setValues("1", "Alice", "85") );
		dataModel.addRow( Row.newBuilder(colModel).setValues("2", "Bob", "95") );
		dataModel.addRow( Row.newBuilder(colModel).setValues("3", "Charlie", "60") );
	
		// Filter score >= 80, sorted DescNullLast
		DataModelView view = dataModel.createView("HighScores",
				new QueryDefinition(
				FilterExpression.newBuilder().begin(
						BinaryCondition.GreaterThanEqual(Operand.of(colModel.getColumn("SCORE")), Operand.of(80))
				).build(),
				new SorterExpressionBuilder(colModel).DescNullLast("SCORE").build()));

		assertEquals(2, view.getRowsCount());
		assertEquals("Bob", view.getRow(0).getFields()[colModel.getColumn("NAME").getId()].toString());
		assertEquals("Alice", view.getRow(1).getFields()[colModel.getColumn("NAME").getId()].toString());
	}
}
