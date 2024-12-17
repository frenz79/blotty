package com.blotty.core.modifiers.sorters;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.blotty.core.models.IRowsModel;
import com.blotty.core.models.Row;
import com.blotty.core.types.IGenericField;

public class SorterConditionChain implements ISorterCondition {

	private final List<SorterExpression> sorters = new LinkedList<>();
	
	@Override
	public boolean apply(IRowsModel rows) {
		Collections.sort(rows.getAllRows(),	new Comparator<Row>() {  
		    @Override  
		    public int compare(Row r1, Row r2) { 
		    	for ( SorterExpression sorter : sorters ) {
		    		int colId = sorter.getColumm().getId();
		    		IGenericField f1 = r1.getFields()[colId];
		    		IGenericField f2 = r2.getFields()[colId];		    		
					int comp = sorter.getOperator().compare(f1, f2);
					if (comp!=0) {
						return comp;
					}
				}
		    	return 0;
		    }  
		});		
		return true;
	}
	
	public boolean addSorter( SorterExpression expr ) {
		return sorters.add(expr);
	}

	public List<SorterExpression> getSorters() {
		return sorters;
	}

	@Override
	public String toString() {
		return "SorterConditionChain [sorters=" + sorters + "]";
	}

}
