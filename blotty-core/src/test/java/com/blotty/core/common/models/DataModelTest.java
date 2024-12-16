package com.blotty.core.common.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.blotty.core.common.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.common.models.modifiers.filters.conditions.Operand;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.impl.StringField;

class DataModelTest {

	@Test
	void testAddAndStreamRows() throws Exception {
		ColumnsModel colModel = new ColumnsModelBuilder()
			// Adding a column providing the Column instance
			.add( new Column("COL_1", FieldType.STRING_TYPE) )
			// ..or just giving the name and the type
			.add( "COL_2", FieldType.STRING_TYPE )
			.build();
		
		// Adding rows using addRow from model
		DataModel dataModel = (DataModel) new DataModel( colModel )
			.addRow( new Row("KEY_1", colModel)
				.set(colModel.getColumn("COL_1"), "V1_1")
				.set(colModel.getColumn("COL_2"), "V1_2")
			)
			.addRow( new Row("KEY_2", colModel)
				.set(colModel.getColumn("COL_1"), "V2_1")
				.set(colModel.getColumn("COL_2"), "V2_2")
			)
			.addRow( new Row("KEY_3", colModel)
				.set(colModel.getColumn("COL_1"), StringField.of("V3_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V3_2"))
			)			
		;
		
		// Adding rows using the RowBuilder
		dataModel.getRowBuilder()
			.newRow("KEY_4")
				.set("COL_1", "V4_1")
				.addToModel()
			.newRow("KEY_5")
				.set("COL_1", "V5_1")
				.addToModel();
		
		assertEquals(5, dataModel.getRowsCount());
		
		final CountDownLatch l1 = new CountDownLatch(5);
		dataModel.stream( new Consumer<Row>() {

			@Override
			public boolean stream(Row item) {
				l1.countDown();
				return true;
			}			
		});
		
		l1.await(1, TimeUnit.SECONDS);
		assertEquals(0l, l1.getCount());
		
		final CountDownLatch l2 = new CountDownLatch(4);
		dataModel.stream( new Consumer<Row>() {

			@Override
			public boolean stream(Row item) {
				l2.countDown();
				return false;
			}			
		});
		
		l2.await(1, TimeUnit.SECONDS);
		assertEquals(3l, l2.getCount());
	}

	@Test
	void testCreateView() throws Exception {
		ColumnsModel colModel = new ColumnsModelBuilder()
			.add( "COL_1", FieldType.STRING_TYPE )
			.add( "COL_2", FieldType.STRING_TYPE )
			.build();
			
		DataModel dataModel = new DataModel( colModel )
			.getRowBuilder()
			.newRow("K1").set("COL_1", "1").addToModel()
			.newRow("K2").set("COL_1", "2").addToModel()
			.newRow("K3").set("COL_1", "3").addToModel()
			.newRow("K4").set("COL_1", "4").addToModel()
			.newRow("K5").set("COL_1", "5").addToModel()
			.getDataModel();
		
		FilterExpression filter = new FilterExpressionBuilder()
			.begin(	BinaryCondition.Equal(Operand.of(colModel.getColumn("COL_1")), Operand.of("2")))
			.or( BinaryCondition.Equal(Operand.of(colModel.getColumn("COL_1")), Operand.of("3")))
			.build();
		
		DataModelView view = dataModel.createView("2_or_3", filter);
		
		assertEquals(5, dataModel.getRowsCount() );
		assertEquals(2, view.getRowsCount() );
	}
}
