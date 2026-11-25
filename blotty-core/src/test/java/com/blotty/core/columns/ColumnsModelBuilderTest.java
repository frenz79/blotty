package com.blotty.core.columns;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.columns.ColumnsModel.Builder;
import com.blotty.core.commons.exceptions.ColumnsModelException;
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
