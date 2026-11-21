package com.blotty.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.IConsumer;
import com.blotty.core.models.storage.InMemoryRowsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.StringField;

class DataModelTest {

	@Test
	void testAddAndStreamRows() throws Exception {
		ColumnsModel colModel = ColumnsModel.newBuilder()
			.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("COL_1").type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("COL_2").type(FieldType.STRING_TYPE)
		).build();
				
		// Adding rows using addRow from model
		DataModel dataModel = (DataModel) new DataModel( new InMemoryRowsModel(colModel) )
			.addRow( new Row(colModel)
				.set(colModel.getKey(), "KEY_1")
				.set(colModel.getColumn("COL_1"), "V1_1")
				.set(colModel.getColumn("COL_2"), "V1_2")
			)
			.addRow( new Row(colModel)
				.set(colModel.getKey(), "KEY_2")
				.set(colModel.getColumn("COL_1"), "V2_1")
				.set(colModel.getColumn("COL_2"), "V2_2")
			)
			.addRow( new Row(colModel)
				.set(colModel.getKey(), "KEY_3")
				.set(colModel.getColumn("COL_1"), StringField.of("V3_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V3_2"))
			)			
		;
		
		// Adding rows using the RowBuilder
		dataModel.addRow(Row.newBuilder(colModel)
			.set("PK", "KEY_4")
			.set("COL_1", "V4_1"));
		dataModel.addRow(Row.newBuilder(colModel)
			.set("PK", "KEY_5")
			.set("COL_1", "V5_1"));
		
		assertEquals(5, dataModel.getRowsCount());
		
		final CountDownLatch l1 = new CountDownLatch(5);
		dataModel.stream( new IConsumer<Row>() {

			@Override
			public boolean stream(Row item) {
				l1.countDown();
				return true;
			}			
		});
		
		l1.await(1, TimeUnit.SECONDS);
		assertEquals(0l, l1.getCount());
		
		final CountDownLatch l2 = new CountDownLatch(4);
		dataModel.stream( new IConsumer<Row>() {

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
		ColumnsModel colModel = ColumnsModel.newBuilder()
			.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("COL_1").type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("COL_2").type(FieldType.STRING_TYPE)
		).build();
			
		DataModel dataModel = (DataModel) new DataModel( new InMemoryRowsModel(colModel) )
			.addRow(Row.newBuilder(colModel).set("PK", "K1").set("COL_1", "1"))
			.addRow(Row.newBuilder(colModel).set("PK", "K2").set("COL_1", "2"))
			.addRow(Row.newBuilder(colModel).set("PK", "K3").set("COL_1", "3"))
			.addRow(Row.newBuilder(colModel).set("PK", "K4").set("COL_1", "4"))
			.addRow(Row.newBuilder(colModel).set("PK", "K5").set("COL_1", "5"));
		
		FilterExpression filter = new FilterExpressionBuilder()
			.begin(	BinaryCondition.Equal(Operand.of(colModel.getColumn("COL_1")), Operand.of("2")))
			.or( BinaryCondition.Equal(Operand.of(colModel.getColumn("COL_1")), Operand.of("3")))
			.build();
		
		DataModelView view = dataModel.createView("2_or_3", filter);
		
		assertEquals(5, dataModel.getRowsCount() );
		assertEquals(2, view.getRowsCount() );
	}
}
