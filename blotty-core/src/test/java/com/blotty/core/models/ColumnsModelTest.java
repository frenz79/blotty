package com.blotty.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blotty.core.models.ColumnsModel.Builder;
import com.blotty.core.types.FieldType;

class ColumnsModelTest {

	@Test
	void columnsModelCreationTest() throws Exception {
		Column col1 = Column.newBuilder("COL_A").type(FieldType.STRING_TYPE).build();
		Column col2 = Column.newBuilder("COL_B").type(FieldType.STRING_TYPE).build();
		
		Builder builder = ColumnsModel.newBuilder();
		builder.add(Column.newBuilder("PK", true).type(FieldType.STRING_TYPE));
		builder.add(col1);
		builder.add(col2);
		
		ColumnsModel colModel = builder.build();
		
		assertEquals(3, colModel.getColumnsCount());
		assertEquals(col1, colModel.getColumn("COL_A"));
		assertEquals(col2, colModel.getColumn("COL_B"));
	}

}
