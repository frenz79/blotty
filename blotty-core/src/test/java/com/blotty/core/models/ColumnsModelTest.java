package com.blotty.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blotty.core.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.types.FieldType;

class ColumnsModelTest {

	@Test
	void columnsModelCreationTest() throws Exception {
		Column col1 = new Column("COL_1", FieldType.STRING_TYPE);
		Column col2 = new Column("COL_2", FieldType.STRING_TYPE);
		
		ColumnsModel colModel = new ColumnsModelBuilder()
				.addKey( "PK", FieldType.STRING_TYPE )
				.add( col1 )
				.add( col2 )
				.build();
		
		assertEquals(3, colModel.getColumnsCount());
		assertEquals(col1, colModel.getColumn("COL_1"));
		assertEquals(col2, colModel.getColumn("COL_2"));
	}

}
