package com.blotty.core.common.parsers;

import com.blotty.core.common.exceptions.FilterExpressionException;
import com.blotty.core.common.exceptions.RowsTypeException;
import com.blotty.core.common.models.ColumnsModel;
import com.blotty.core.common.models.modifiers.filters.FilterExpression;
import com.blotty.core.common.models.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.common.models.modifiers.filters.IFilterCondition;
import com.blotty.core.common.models.modifiers.filters.conditions.IOperator;
import com.blotty.core.common.models.modifiers.filters.conditions.Operand;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.LesserThanEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.common.models.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.common.models.modifiers.filters.conditions.unary.IUnaryOperator;
import com.blotty.core.common.models.modifiers.filters.conditions.unary.UnaryCondition;

public class SQLQueryParser {

	/** 
	 *  Process a string containing an SQL like syntax:     
	 *  - every column name must be delimited by " character
	 *  - every constant valies must be delimited by ' character
  	 *  - const vs const comparison is not allowed
     	 *  - supported operators are: =, !=, >, <, >=, <=, LIKE, IS NULL, IS NOT NULL
	 *  - supported conjuctions are: AND , OR
	 *  
  	 *  Example:
         *  "COLUMN_1" = 'example value' OR "COLUMN_2" LIKE 'xxx' AND "COLUMN_3" >= "COLUMN_4"
	 */
	
	static enum OperandType {
		CONST,
		COLUMN
	}

	static enum Conjunction {
		AND,
		OR
	}
	
	public FilterExpression parse( ColumnsModel colsModel, String query ) throws FilterExpressionException, RowsTypeException {
		FilterExpressionBuilder builder = null;
		
		query = query.trim();
		
		// 0 : waiting for left operand
		// 1 : waiting for operator
		// 2 : waiting for right operand
		// 3 : waiting for conjunction
		int step = 0; 
		
		OperandType leftOperandType = null;
		String leftOperand = null;
		
		OperandType rightOperandType = null;
		String rightOperand = null;
		
		IOperator operator = null;
		
		Conjunction conjunction = null;
		
		for ( int i=0; i<query.length(); i++ ) {
			char ch = query.charAt(i);
	
			if ( ch==' ' ) {
				continue;
			}
			
			OperandType operandType = null;
			String operand = null;
						
			if (ch=='"') {	// Column name					
				operandType = OperandType.COLUMN;
				operand = readStringUntil('\"', i+1,query);
				i += operand.length()+1;
			} else if (ch=='\'') {	// Const Value	
				operandType = OperandType.CONST;
				operand = readStringUntil('\'', i+1,query);
				i += operand.length()+1;
			} 			
			if ( operandType!=null ) {
				if ( step==0 ) {
					leftOperandType = operandType;
					leftOperand = operand;
				} else if ( step==2 ) {
					rightOperandType = operandType;
					rightOperand = operand;
				} else {
					throw new RuntimeException("Invalid Syntax!");
				}
				step++;
				continue;				
			}
			
			if ( step==1 ) {
				char nextCh = Character.toUpperCase(query.charAt(i+1));
				operator = buildOperator( ch, nextCh );
				
				if ( operator==null ) {				
					if ( ch=='I' && nextCh=='S' ) {
						// unary op here!
						// no need for step 2
						step++; 
					}
				}
				step++;
			}
			
			if ( step==3 ) {
				for ( int j=i; j<query.length(); j++ ) {
					char chj=Character.toUpperCase(query.charAt(j));
					char chjj=Character.toUpperCase(query.charAt(j+1));
					
					if ( chj=='A' && chjj=='N' ) {
						conjunction = Conjunction.AND;
						i = j+2;
						break;
					}
					else if ( chj=='O' && chjj=='R' ) {
						conjunction = Conjunction.OR;
						i = j+1;
						break;
					}
				}
				
				IFilterCondition filterCondition= null;
				if ( rightOperand==null ) {
					filterCondition = new UnaryCondition(null, (IUnaryOperator)operator);
				} else {
					filterCondition = buildBinaryCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
				}
				
				if ( builder==null ) {
					builder = new FilterExpressionBuilder().begin( filterCondition );
				} else {
					addConjunction( builder, filterCondition, conjunction );
				}
				
				operator = null;
				step = 0;
			}
		}
		
		IFilterCondition filterCondition= null;
		if ( rightOperand==null ) {
			filterCondition = new UnaryCondition(null, (IUnaryOperator)operator);
		} else {
			filterCondition = buildBinaryCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
		}
		
		addConjunction( builder, filterCondition, conjunction );		
		return builder.build();
	}
	
	private FilterExpressionBuilder addConjunction( FilterExpressionBuilder builder, IFilterCondition filterCondition, Conjunction conjunction ) throws FilterExpressionException {
		switch ( conjunction ) {
		case AND:
			builder.and(filterCondition);
			break;
		case OR:
			builder.or(filterCondition);
			break;					
		}
		return builder;
	}
	
	private IOperator buildOperator( char ch1, char ch2 ) {			
		if ( ch1=='!' && ch2=='=' ) {
			return new NotEqual();
		}				
		else if ( ch1=='>') {
			return (ch2=='=')? new GreaterThanEqual():new GreaterThan();
		}
		else if ( ch1=='<') {
			return (ch2=='=')? new LesserThanEqual():new LesserThan();			
		}
		else if ( ch1=='=' ) {
			return new Equal();
		}
		else if (Character.toUpperCase(ch1)=='L' && Character.toUpperCase(ch2)=='I' ) {
			return new Like();
		}
		return null;
	}
	
	private IFilterCondition buildBinaryCondition( 
			ColumnsModel colsModel,
			String leftOperand,
			OperandType leftOperandType,
			String rightOperand,			
			OperandType rightOperandType,
			IOperator operator ) throws RowsTypeException {
		
		if ( OperandType.CONST.equals(rightOperandType) && OperandType.CONST.equals(leftOperandType) ) {
			throw new RuntimeException("Invalid const to const expression");
		}		
		if ( OperandType.CONST.equals(leftOperandType) ) {
			return new BinaryCondition(
				Operand.of(leftOperand, colsModel.getColumn(rightOperand)), 
				(IBinaryOperator)operator, 
				Operand.of(colsModel.getColumn(rightOperand))
			);
		} 
		if ( OperandType.CONST.equals(rightOperandType) ) {
			return new BinaryCondition(
				Operand.of(colsModel.getColumn(leftOperand)),
				(IBinaryOperator)operator, 
				Operand.of(rightOperand, colsModel.getColumn(leftOperand) )
			);
		} 
		return new BinaryCondition(
			Operand.of(colsModel.getColumn(leftOperand)),
			(IBinaryOperator)operator, 
			Operand.of(colsModel.getColumn(rightOperand))
		);
	}
	
	private String readStringUntil(char endChar, int i, String str) {
		return str.substring(i, str.indexOf(endChar, i));
	}
}
