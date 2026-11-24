package com.blotty.core.models.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
import com.blotty.core.models.rows.Row;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.StringField;

class InMemoryRowsModelTest {

	private ColumnsModel colModel;
	private InMemoryRowsModel rowsModel;

	@BeforeEach
	void setUp() throws ColumnsModelException {
		colModel = ColumnsModel.newBuilder()
			.add(Column.newBuilder("ID", true).type(FieldType.STRING_TYPE))
			.add(Column.newBuilder("NAME").type(FieldType.STRING_TYPE)
		).build();
		rowsModel = new InMemoryRowsModel(colModel);
	}

	@Test
	void testAddAndGetRows() throws RowsModelException, RowsTypeException {
		Row r1 = Row.newBuilder(colModel).set(colModel.getKey(), "1").set(colModel.getColumn("NAME"), "Alice").build();
		Row r2 = Row.newBuilder(colModel).set(colModel.getKey(), "2").set(colModel.getColumn("NAME"), "Bob").build();

		rowsModel.addRow(r1);
		rowsModel.addRow(r2);

		assertEquals(2, rowsModel.getRowsCount());

		assertEquals(r1, rowsModel.getRow(0));
		assertEquals(r2, rowsModel.getRow(1));

		assertEquals(r1, rowsModel.getRow(StringField.of("1")));
		assertEquals(r2, rowsModel.getRow(StringField.of("2")));
	}

	@Test
	void testDuplicateKeyException() throws RowsModelException, RowsTypeException {
		Row r1 = Row.newBuilder(colModel).set(colModel.getKey(), "1").set(colModel.getColumn("NAME"), "Alice").build();
		Row rDuplicate = Row.newBuilder(colModel).set(colModel.getKey(), "1").set(colModel.getColumn("NAME"), "Alice Clone").build();

		rowsModel.addRow(r1);
		assertThrows(RowsModelException.class, () -> rowsModel.addRow(rDuplicate));

		// Ensure map and list integrity
		assertEquals(1, rowsModel.getRowsCount());
		assertEquals("Alice", rowsModel.getRow(StringField.of("1")).getFields()[colModel.getColumn("NAME").getId()].toString());
	}
}
