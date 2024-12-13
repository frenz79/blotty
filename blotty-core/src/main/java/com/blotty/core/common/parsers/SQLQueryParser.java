package com.blotty.core.common.parsers;

import com.blotty.core.common.models.modifiers.filters.FilterExpression;

public class SQLQueryParser {

	/**
	 *  SELECT *
	 *    FROM <MODEL_ID>
	 *    WHERE "COL1" = 10
	 *      AND "COL2" = 'PIPPO'
	 *      OR  "COL3" IS NULL;
	 */
	public FilterExpression parse( String query ) {
		
		
		return null;
	}
}
