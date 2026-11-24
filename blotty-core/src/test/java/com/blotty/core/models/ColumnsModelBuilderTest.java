package com.blotty.core.models;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
import com.blotty.core.models.columns.ColumnsModel.Builder;
import com.blotty.core.types.FieldType;

class ColumnsModelBuilderTest {

	@Test
	void testDuplicateColumnThrowsException() {
		Builder builder = ColumnsModel.newBuilder();
		assertThrows(ColumnsModelException.class, () -> {
			builder.add(Column.newBuilder("COL_A", true).type(FieldType.STRING_TYPE));
			builder.add(Column.newBuilder("COL_A").type(FieldType.STRING_TYPE));
		});
	}

	@Test
	void testMissingKeyThrowsException() {
		Builder builder = ColumnsModel.newBuilder();
		assertThrows(ColumnsModelException.class, () -> {
			builder.add(Column.newBuilder("COL_A").type(FieldType.STRING_TYPE));
			builder.build();
		});
	}

	@Test
	void testMultipleKeysThrowsException() {
		Builder builder = ColumnsModel.newBuilder();
		assertThrows(ColumnsModelException.class, () -> {
			builder.add(Column.newBuilder("KEY_1", true).type(FieldType.STRING_TYPE));
			builder.add(Column.newBuilder("KEY_2", true).type(FieldType.STRING_TYPE));
		});
	}
}
