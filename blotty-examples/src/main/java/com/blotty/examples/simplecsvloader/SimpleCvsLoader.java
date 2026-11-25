package com.blotty.examples.simplecsvloader;

import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.blotty.core.DataModel;
import com.blotty.core.DataModelView;
import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.parsers.SQLQueryParser;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.rows.Row;
import com.blotty.core.storage.InMemoryRowsModel;
import com.blotty.core.types.FieldType;

public class SimpleCvsLoader {

	public static void main( String args[] ) throws Exception {		
		ColumnsModel colModel = ColumnsModel.newBuilder()
				.add(Column.newBuilder("Index", true).type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("Organization Id").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("Name").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("Website").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("Country").type(FieldType.STRING_TYPE).cacheable())
				.add(Column.newBuilder("Description").type(FieldType.STRING_TYPE))
				.add(Column.newBuilder("Founded").type(FieldType.SHORT_TYPE))
				.add(Column.newBuilder("Industry").type(FieldType.STRING_TYPE).cacheable())
				.add(Column.newBuilder("Number of employees").type(FieldType.INTEGER_TYPE))				
				.build();
		
		DataModel dataModel = new DataModel( new InMemoryRowsModel(colModel) );
		
		Reader in = new FileReader("src/test/resources/organizations-10000.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int rowNumber = 0;
		for (CSVRecord record : records) {
			if ( rowNumber==0 ) {
				rowNumber++;
				continue;
			}
			dataModel.addRow(Row.newBuilder(colModel).setValues(record.values()));			
		}		
		
		in.close();
		
		System.out.println("Model size:"+dataModel.getRowsCount());
				
		FilterExpression filter = FilterExpression.newBuilder()
			.begin(	BinaryCondition.Equal(Operand.of(colModel.getColumn("Country")), Operand.of("Italy"))) 
			.and( BinaryCondition.GreaterThanEqual(Operand.of(colModel.getColumn("Number of employees")), Operand.of(5000))) 
			.and( BinaryCondition.Like(Operand.of(colModel.getColumn("Industry")), Operand.of("Engineering")))
		.build();
		
		DataModelView view = dataModel.createView("ItalianOrganizations", new QueryDefinition(filter,null));
		
		System.out.println(view.dumpToString());
		
		DataModelView sqlFilteredView = dataModel.createView("ItalianOrganizations_1", 
			new SQLQueryParser().parse(colModel, " \"Country\" = 'Italy' AND \"Number of employees\" >= '5000' AND \"Industry\" LIKE 'Engineering' ")
		);
				
		System.out.println(sqlFilteredView.dumpToString());
						
		sqlFilteredView.sort(
			new SorterExpressionBuilder(colModel)
				.AscNullLast("Number of employees") 
				.DescNullLast("Industry")
				.build()	
		);
		
		System.out.println(sqlFilteredView.dumpToString());
	}
}
