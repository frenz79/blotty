package com.blotty.core.common.parsers;

import com.blotty.core.common.exceptions.FilterExpressionException;
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
	 *      "COL1" = 10
	 *      AND "COL2" = 'PIPPO'
	 *      OR  "COL3" IS NULL
	 *      OR "COL3" = "COL4"
	 *      ;      
	 *      
	 *      " : column name delimiter
	 *      ' : constant value delimiter
	 *      AND , OR, =, !=, >, <, >=, <=, LIKE, IS NULL, IS NOT NULL
	 *      
	 */
	
	static enum OperandType {
		CONST,
		COLUMN
	}

	static enum Conjunction {
		AND,
		OR
	}
	
	public FilterExpression parse( ColumnsModel colsModel, String query ) throws FilterExpressionException {
		FilterExpressionBuilder builder = null;
		
		query = query.trim().toUpperCase();
		
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
				char nextCh = query.charAt(i+1);
				
				if ( ch=='=' ) {
					operator = new Equal();
				}				
				else if ( ch=='!' && nextCh=='=' ) {
					operator = new NotEqual();
				}				
				else if ( ch=='>') {
					if (  nextCh=='=' ) {
						operator = new GreaterThanEqual();
					} else {
						operator = new GreaterThan();
					}
				}
				else if ( ch=='<') {
					if (  nextCh=='=' ) {
						operator = new LesserThan();
					} else {
						operator = new LesserThanEqual();
					}				
				}
				else if ( ch=='L' && nextCh=='I' ) {
					operator = new Like();
				}
								
				else if ( ch=='I' && nextCh=='S' ) {
					// unary op here!
					// no need for step 2
					step++; 
				}
				step++;
			}
			
			if ( step==3 ) {
				for ( int j=i; j<query.length(); j++ ) {
					if ( query.charAt(j)=='A' && query.charAt(j+1)=='N' ) {
						conjunction = Conjunction.AND;
						i = j+2;
						break;
					}
					else if ( query.charAt(j)=='O' && query.charAt(j+1)=='R' ) {
						conjunction = Conjunction.OR;
						i = j+1;
						break;
					}
				}
				
				IFilterCondition filterCondition= null;
				if ( rightOperand==null ) {
					filterCondition = new UnaryCondition(null, (IUnaryOperator)operator);
				} else {
					filterCondition = buildCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
				}
				
				if ( builder==null ) {
					builder = new FilterExpressionBuilder().begin( filterCondition );
				} else {
					switch ( conjunction ) {
					case AND:
						builder.and(filterCondition);
						break;
					case OR:
						builder.or(filterCondition);
						break;					
					}
				}
				
				operator = null;
				step = 0;
			}
		}
		return builder.build();
	}
	
	
	private IFilterCondition buildCondition( 
			ColumnsModel colsModel,
			String leftOperand,
			OperandType leftOperandType,
			String rightOperand,			
			OperandType rightOperandType,
			IOperator operator ) {
		
		if ( OperandType.CONST.equals(leftOperandType) ) {
			if ( OperandType.CONST.equals(rightOperandType) ) {
				throw new RuntimeException("Invalid const to const expression");
			}
			return new BinaryCondition(
				Operand.of(leftOperand), 
				(IBinaryOperator)operator, 
				Operand.of(colsModel.getColumn(rightOperand))
			);
		} 
		else if ( OperandType.CONST.equals(rightOperandType) ) {
			if ( OperandType.CONST.equals(leftOperandType) ) {
				throw new RuntimeException("Invalid const to const expression");
			}
			return new BinaryCondition(
				Operand.of(colsModel.getColumn(leftOperand)),
				(IBinaryOperator)operator, 
				Operand.of(rightOperand)
			);
		}
		
		throw new RuntimeException("Cannot build any condition");
	}
	
	private String readStringUntil(char endChar, int i, String str) {
		return str.substring(i, str.indexOf(endChar, i));
	}
}
