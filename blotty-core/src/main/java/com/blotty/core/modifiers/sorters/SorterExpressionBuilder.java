package com.blotty.core.modifiers.sorters;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.modifiers.sorters.operators.AscNullFirst;
import com.blotty.core.modifiers.sorters.operators.AscNullLast;
import com.blotty.core.modifiers.sorters.operators.DescNullFirst;
import com.blotty.core.modifiers.sorters.operators.DescNullLast;
import com.blotty.core.modifiers.sorters.operators.ISorterOperator;

public class SorterExpressionBuilder {

	private final ColumnsModel colModel;
	private final SorterConditionChain sorterChain;
	
	public SorterExpressionBuilder( ColumnsModel colModel ) {
		this.colModel = colModel;
		this.sorterChain = new SorterConditionChain();
	}
	
	public SorterExpressionBuilder add( SorterExpression sorter ) throws FilterExpressionException {
		sorterChain.addSorter(sorter);
		return this;
	}
	
	public SorterExpressionBuilder add( Column columm, ISorterOperator operator ) throws FilterExpressionException {
		return add( new SorterExpression(columm,operator));
	}
	
	public SorterExpressionBuilder add( String colummName, ISorterOperator operator ) throws FilterExpressionException {
		return add( colModel.getColumn(colummName), operator );
	}
	
	private static final ISorterOperator ASC_NULL_FIRST = new AscNullFirst();
	private static final ISorterOperator ASC_NULL_LAST = new AscNullLast();
	private static final ISorterOperator DESC_NULL_FIRST = new DescNullFirst();
	private static final ISorterOperator DESC_NULL_LAST = new DescNullLast();
	
	public SorterExpressionBuilder AscNullFirst( String colummName ) throws FilterExpressionException {
		return add( colModel.getColumn(colummName), ASC_NULL_FIRST );
	}
	
	public SorterExpressionBuilder AscNullLast( String colummName ) throws FilterExpressionException {
		return add( colModel.getColumn(colummName), ASC_NULL_LAST );
	}
	
	public SorterExpressionBuilder DescNullFirst( String colummName ) throws FilterExpressionException {
		return add( colModel.getColumn(colummName), DESC_NULL_FIRST );
	}
	
	public SorterExpressionBuilder DescNullLast( String colummName ) throws FilterExpressionException {
		return add( colModel.getColumn(colummName), DESC_NULL_LAST );
	}
	
	public SorterConditionChain build() {
		return sorterChain;
	}
}
