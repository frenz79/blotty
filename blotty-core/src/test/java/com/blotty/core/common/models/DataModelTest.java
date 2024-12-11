package com.blotty.core.common.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.blotty.core.common.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.common.models.commons.Consumer;
import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.impl.StringField;

class DataModelTest {

	@Test
	void testAddAndStreamRows() throws Exception {
		ColumnsModel colModel = new ColumnsModelBuilder()
			.add( new Column("COL_1", FieldType.STRING_TYPE) )
			.add( new Column("COL_2", FieldType.STRING_TYPE) )
			.build();
		
		DataModel dataModel = new DataModel( colModel )
			.addRow( new Row("KEY_1", colModel)
				.set(colModel.getColumn("COL_1"), StringField.of("V1_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V1_2"))
			)
			.addRow( new Row("KEY_2", colModel)
				.set(colModel.getColumn("COL_1"), StringField.of("V2_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V2_2"))
			)
			.addRow( new Row("KEY_3", colModel)
				.set(colModel.getColumn("COL_1"), StringField.of("V3_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V3_2"))
			)
			.addRow( new Row("KEY_4", colModel)
				.set(colModel.getColumn("COL_1"), StringField.of("V4_1"))
				.set(colModel.getColumn("COL_2"), StringField.of("V4_2"))
			)
		;
		
		assertEquals(4, dataModel.getRowsCount());
		
		final CountDownLatch l1 = new CountDownLatch(4);
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

}
