package com.blotty.core.rows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.blotty.core.DataModel;
import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.storage.InMemoryRowsModel;
import com.blotty.core.types.FieldType;

class RowBuilderTest {

	@Test
	void testRowBuilder() throws Exception {
		ColumnsModel colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("ID", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("FIRST_NAME").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("LAST_NAME").type(FieldType.STRING_TYPE)
			).build();
	
		DataModel model = new DataModel(new InMemoryRowsModel(colModel));
		model.addRow( Row.newBuilder(colModel).setValues("1", "John", "Doe") );
		model.addRow( Row.newBuilder(colModel).set("ID", "2").set("FIRST_NAME", "Jane").set("LAST_NAME", "Smith") );

		// Duplicate key should throw RowsModelException
		assertThrows(RowsModelException.class, () -> model.addRow( Row.newBuilder(colModel).setValues("1", "Duplicate", "User") ));

		// Invalid values count should throw RowsModelException
		assertThrows(RowsModelException.class, () -> Row.newBuilder(colModel).setValues("John", "Doe"));

		assertEquals(2, model.getRowsCount());

		Row r1 = model.getRowsModel().getRow(0);
		Row r2 = model.getRowsModel().getRow(1);

		assertEquals("1", r1.getFields()[0].toString());
		assertEquals("John", r1.getFields()[1].toString());
		assertEquals("Doe", r1.getFields()[2].toString());

		assertEquals("2", r2.getFields()[0].toString());
		assertEquals("Jane", r2.getFields()[1].toString());
		assertEquals("Smith", r2.getFields()[2].toString());
	}
}
