package com.blotty.examples.graphicalcsvexplorer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.blotty.core.commons.exceptions.ColumnsModelException;
import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.ColumnsModel.ColumnsModelBuilder;
import com.blotty.core.models.DataModel;
import com.blotty.core.models.DataModelView;
import com.blotty.core.models.RowBuilder;
import com.blotty.core.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.sorters.SorterExpressionBuilder;
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
	
	public void loadCsv(File selectedFile) throws ColumnsModelException, IOException, RowsTypeException, RowsModelException {
		Reader in = new FileReader(selectedFile);
		ColumnsModel colModel = null;
		RowBuilder rowBuilder = null;
		
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		int recordNumber = 0;
		for (CSVRecord record : records) {
			if ( recordNumber==0 ) {
				ColumnsModelBuilder colModelBuilder = new ColumnsModelBuilder();
				
				// Header : col=0 is the key
				for ( int i=0; i<record.size(); i++ ) {
					if (i==0) {
						colModelBuilder.addKey( record.get(i), FieldType.STRING_TYPE);
					} else {
						colModelBuilder.add( new Column(record.get(i), FieldType.STRING_TYPE));
					}
				}
				colModel = colModelBuilder.build();
				dataModel = new DataModel( colModel );
				rowBuilder = dataModel.getRowBuilder();
				view.setTableColumns( colModel );
			} else {
				rowBuilder.newRow();
				for ( int i=0; i<record.size(); i++ ) {
					rowBuilder.set(colModel.getColumn(i), record.get(i));
				}
				rowBuilder.addToModel();
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
			(ascending)?
				new SorterExpressionBuilder(dataModel.getColumnsModel())
					.AscNullLast(col.getName()) 
					.build()
				:
				new SorterExpressionBuilder(dataModel.getColumnsModel())
					.DescNullLast(col.getName()) 
					.build()
		);
		
		view.setTableData( sortedView );
	}

	public void applyFilter(String colName, String operator, String value) throws RowsModelException {	
		FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
		
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
		
		DataModelView filteredView = dataModel.createView("ItalianOrganizations", filterBuilder.build());
		view.setTableData( filteredView );
	}
}
