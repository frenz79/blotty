package com.blotty.examples;

import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.blotty.core.common.models.ColumnsModel;
import com.blotty.core.common.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.common.models.modifiers.filters.conditions.Equal;
import com.blotty.core.common.models.DataModel;
import com.blotty.core.common.models.DataModelView;
import com.blotty.core.common.models.RowBuilder;
import com.blotty.core.common.models.types.FieldType;
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
				.add( "Number of employees", FieldType.STRING_TYPE )				
				.build();
		
		DataModel dataModel = new DataModel( colModel );
		RowBuilder rowBuilder = dataModel.getRowBuilder();
		
		Reader in = new FileReader("src/main/resources/organizations-10000.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		for (CSVRecord record : records) {
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
		
		in.close();
		
		System.out.println("Model size:"+dataModel.getRowsCount());
				
		FilterExpression filter = new FilterExpressionBuilder()
				.begin(	new Equal(colModel.getColumn("Country"), StringField.of("Italy")) )
				.build();
		
		DataModelView view = dataModel.createView("ItalianOrganizations", filter);
		
		System.out.println(view.dumpToString());
	}
}
