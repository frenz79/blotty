package com.blotty.examples;

import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.blotty.core.common.models.ColumnsModel;
import com.blotty.core.common.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.common.models.DataModel;
import com.blotty.core.common.models.DataModelView;
import com.blotty.core.common.models.RowBuilder;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.common.models.modifiers.filters.conditions.Operand;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.common.models.types.FieldType;
import com.blotty.core.common.models.types.impl.IntegerField;
import com.blotty.core.common.models.types.impl.StringField;

public class SimpleCvsLoader {

	public static void main( String args[] ) throws Exception {		
		ColumnsModel colModel = new ColumnsModelBuilder()
				.add( "Organization Id", FieldType.STRING_TYPE )
				.add( "Name", FieldType.STRING_TYPE )
				.add( "Website", FieldType.STRING_TYPE )
				.add( "Country", FieldType.STRING_TYPE )
				.add( "Description", FieldType.STRING_TYPE )
				.add( "Founded", FieldType.STRING_TYPE )
				.add( "Industry", FieldType.STRING_TYPE )
				.add( "Number of employees", FieldType.INTEGER_TYPE )				
				.build();
		
		DataModel dataModel = new DataModel( colModel );
		RowBuilder rowBuilder = dataModel.getRowBuilder();
		
		Reader in = new FileReader("src/main/resources/organizations-10000.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int rowNumber = 0;
		for (CSVRecord record : records) {
			if ( rowNumber>0 ) {
				rowBuilder.newRow(record.get(0))
					.set("Organization Id", record.get(1))
					.set("Name", record.get(2))
					.set("Website", record.get(3))
					.set("Country", record.get(4))
					.set("Description", record.get(5))
					.set("Founded", record.get(6))
					.set("Industry", record.get(7))
					.set("Number of employees", record.get(8))	
				.addToModel();
			}
			rowNumber++;
		}		
		
		in.close();
		
		System.out.println("Model size:"+dataModel.getRowsCount());
				
		FilterExpression filter = new FilterExpressionBuilder()
			.begin(	BinaryCondition.Equal(Operand.of(colModel.getColumn("Country")), Operand.of("Italy"))) 
			.and( BinaryCondition.GreaterThanEqual(Operand.of(colModel.getColumn("Number of employees")), Operand.of(5000))) 
			.and( BinaryCondition.Like(Operand.of(colModel.getColumn("Industry")), Operand.of("Engineering")))
		.build();
		
		DataModelView view = dataModel.createView("ItalianOrganizations", filter);
		
		System.out.println(view.dumpToString());
	}
}
