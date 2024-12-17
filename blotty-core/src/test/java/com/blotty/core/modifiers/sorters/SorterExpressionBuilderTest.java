package com.blotty.core.modifiers.sorters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.models.DataModel;
import com.blotty.core.models.DataModelView;
import com.blotty.core.parsers.SQLQueryParser;
import com.blotty.core.types.FieldType;
import com.blotty.core.types.impl.IntegerField;
import com.blotty.core.types.impl.NullField;

class SorterExpressionBuilderTest {

	@Test
	void test() throws ColumnsModelException, RowsModelException, RowsTypeException, FilterExpressionException {
		ColumnsModel colModel = new ColumnsModelBuilder()
				.add( "COL_1", FieldType.STRING_TYPE )
				.add( "COL_2", FieldType.STRING_TYPE )
				.add( "COL_3", FieldType.INTEGER_TYPE )		
				.build();

		DataModel dataModel = new DataModel( colModel );

		dataModel.getRowBuilder()
		.newRow("K1")
		.set(colModel.getColumn(0), "A").set(colModel.getColumn(1), "A").set(colModel.getColumn(2), IntegerField.of(1) )
		.addToModel()

		.newRow("K2")
		.set(colModel.getColumn(0), "A").set(colModel.getColumn(1), "B").set(colModel.getColumn(2), IntegerField.of(2) )
		.addToModel()

		.newRow("K3")
		.set(colModel.getColumn(0), "A").set(colModel.getColumn(1), "C").set(colModel.getColumn(2), IntegerField.of(3) )
		.addToModel()

		.newRow("K4")
		.set(colModel.getColumn(0), "B").set(colModel.getColumn(1), "C").set(colModel.getColumn(2), IntegerField.of(3) )
		.addToModel()

		.newRow("K5")
		.set(colModel.getColumn(0), "B").set(colModel.getColumn(1), "C").set(colModel.getColumn(2), NullField.NULL )
		.addToModel()
		;		

		DataModelView sqlFilteredView = dataModel.createView("TEST", 
				new SQLQueryParser().parse(colModel, " \"COL_1\" = 'A' OR \"COL_2\" = 'B' OR \"COL_2\" = 'C' ")
				);

		//System.out.println(sqlFilteredView.dumpToString());

		sqlFilteredView.sort(
				new SorterExpressionBuilder(colModel)
				.DescNullLast("COL_3")
				.build()	
				);	
/*
|COL_1|COL_2|COL_3|
-------------------
|A    |C    |3    |
|B    |C    |3    |
|A    |B    |2    |
|A    |A    |1    |
|B    |C    |NULL |
*/
		System.out.println("DescNullLast\n"+sqlFilteredView.dumpToString());
		assertEquals("K5", sqlFilteredView.getRow(sqlFilteredView.getRowsCount()-1).getKey() );
		
		sqlFilteredView.sort(
				new SorterExpressionBuilder(colModel)
				.DescNullFirst("COL_3")
				.build()	
				);
/*
|COL_1|COL_2|COL_3|
-------------------
|B    |C    |NULL |
|A    |C    |3    |
|B    |C    |3    |
|A    |B    |2    |
|A    |A    |1    | 
*/
		System.out.println("DescNullFirst\n"+sqlFilteredView.dumpToString());
		assertEquals("K5", sqlFilteredView.getRow( 0).getKey() );
		
		sqlFilteredView.sort(
				new SorterExpressionBuilder(colModel)
				.AscNullLast("COL_3")
				.build()	
				);		
/*
|COL_1|COL_2|COL_3|
-------------------
|A    |A    |1    |
|A    |B    |2    |
|A    |C    |3    |
|B    |C    |3    |
|B    |C    |NULL | 
*/
		System.out.println("AscNullLast\n"+sqlFilteredView.dumpToString());
		assertEquals("K5", sqlFilteredView.getRow( sqlFilteredView.getRowsCount()-1).getKey() );
				
		sqlFilteredView.sort(
				new SorterExpressionBuilder(colModel)
				.AscNullFirst("COL_3")
				.build()	
				);	
/*
|COL_1|COL_2|COL_3|
-------------------
|B    |C    |NULL |
|A    |A    |1    |
|A    |B    |2    |
|A    |C    |3    |
|B    |C    |3    |
*/
		System.out.println("AscNullFirst\n"+sqlFilteredView.dumpToString());
		assertEquals("K5", sqlFilteredView.getRow( 0).getKey() );
	}

}
