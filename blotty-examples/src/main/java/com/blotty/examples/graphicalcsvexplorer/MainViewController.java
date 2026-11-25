package com.blotty.examples.graphicalcsvexplorer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.blotty.core.DataModel;
import com.blotty.core.DataModelView;
import com.blotty.core.columns.Column;
import com.blotty.core.columns.ColumnsModel;
import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.indexes.HashIndex;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
import com.blotty.core.query.QueryDefinition;
import com.blotty.core.rows.Row;
import com.blotty.core.rows.Row.Builder;
import com.blotty.core.storage.InMemoryRowsModel;
import com.blotty.core.types.FieldType;

public class MainViewController {

	private MainView view;
	private DataModel dataModel;
	
	public static void main(String[] args) {
		MainViewController ctr = new MainViewController();		
		ctr.setView( new MainView() );
		ctr.getView().setController( ctr );
		ctr.getView().setVisible( true );
	}
	
	public void loadCsv(File selectedFile) throws ColumnsModelException, IOException, RowsTypeException, RowsModelException, FilterExpressionException {
		Reader in = new FileReader(selectedFile);
		ColumnsModel colModel = null;
		
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int recordNumber = 0;
		for (CSVRecord record : records) {
			if ( recordNumber==0 ) {
				ColumnsModel.Builder colModelBuilder = ColumnsModel.newBuilder();
				
				// Header : col=0 is the key
				for ( int i=0; i<record.size(); i++ ) {
					colModelBuilder.add(Column.newBuilder(record.get(i), i==0).type(FieldType.STRING_TYPE).cacheable());
				}
				
				colModel = colModelBuilder.build();
				dataModel = new DataModel( new InMemoryRowsModel(colModel) );
				// 1Million benchmark
				// dataModel.addIndex( new HashIndex(colModel.getColumn("city")));
				view.setTableColumns( colModel );
			} else {
				Builder row = Row.newBuilder(colModel);
				for ( int i=0; i<record.size(); i++ ) {
					row.set(colModel.getColumn(i), record.get(i));
				}
				dataModel.addRow(row);
			}
			recordNumber++;
		}		
		in.close();
       	view.setTableData( dataModel );
	}

	public MainView getView() {
		return view;
	}

	public void setView(MainView view) {
		this.view = view;
	}

	public void applySorting(int colId, boolean ascending) throws RowsModelException, FilterExpressionException {
		Column col = dataModel.getColumnsModel().getColumn(colId);
		
		DataModelView sortedView = dataModel.createView("Sort:"+col.getName(), 
				new QueryDefinition(null,(ascending)?				
				new SorterExpressionBuilder(dataModel.getColumnsModel())
					.AscNullLast(col.getName()) 
					.build()
				:
				new SorterExpressionBuilder(dataModel.getColumnsModel())
					.DescNullLast(col.getName()) 
					.build())
		);
		
		view.setTableData( sortedView );
	}

	public void applyFilter(String colName, String operator, String value) throws RowsModelException, FilterExpressionException {	
		com.blotty.core.modifiers.filters.FilterExpression.Builder filterBuilder = FilterExpression.newBuilder();
		
		if ("=".equals(operator)) {
			filterBuilder.begin( BinaryCondition.Equal(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if ("!=".equals(operator)) {
			filterBuilder.begin( BinaryCondition.NotEqual(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if (">".equals(operator)) {
			filterBuilder.begin( BinaryCondition.GreaterThan(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if (">=".equals(operator)) {
			filterBuilder.begin( BinaryCondition.GreaterThanEqual(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if ("<".equals(operator)) {
			filterBuilder.begin( BinaryCondition.LesserThan(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if ("<=".equals(operator)) {
			filterBuilder.begin( BinaryCondition.LesserThanEqual(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if ("LIKE".equals(operator)) {
			filterBuilder.begin( BinaryCondition.Like(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		else if ("NOT LIKE".equals(operator)) {
			filterBuilder.begin( BinaryCondition.NotLike(Operand.of(dataModel.getColumnsModel().getColumn(colName)), Operand.of(value)));
		}
		long startTime = System.nanoTime();
		DataModelView filteredView = dataModel.createView("ItalianOrganizations", new QueryDefinition( filterBuilder.build(), null));
		long endTime = System.nanoTime();
		System.out.println("Filtering time:"+TimeUnit.NANOSECONDS.toMicros(endTime-startTime));
		view.setTableData( filteredView );
	}
}
