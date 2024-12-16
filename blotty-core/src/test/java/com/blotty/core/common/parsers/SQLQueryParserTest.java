package com.blotty.core.common.parsers;

import org.junit.jupiter.api.Test;

import com.blotty.core.common.exceptions.ColumnsModelException;
import com.blotty.core.common.exceptions.FilterExpressionException;
import com.blotty.core.common.models.ColumnsModel;
import com.blotty.core.common.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.types.FieldType;

class SQLQueryParserTest {

	@Test
	void test() throws ColumnsModelException, FilterExpressionException {
		ColumnsModel colsModel = new ColumnsModelBuilder()
			.add("COL_1", FieldType.STRING_TYPE)
			.add("COL_2", FieldType.STRING_TYPE)
			.add("COL_3", FieldType.STRING_TYPE)
		.build();
		
		FilterExpression parser = new SQLQueryParser().parse(colsModel, " \"COL_1\" = 'XXXX' AND \"COL_2\" > '40' OR \"COL3\" < \"COL_2\" ");
		
		System.out.println("Query: "+parser);
	}

}
