package com.blotty.core.parsers;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.FilterExpressionBuilder;
import com.blotty.core.modifiers.filters.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterOperator;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.modifiers.filters.conditions.unary.IUnaryOperator;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;

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
	    query = query.trim();
	    if (query.isEmpty()) {
	        throw new FilterExpressionException("Empty query");
	    }

	    FilterExpressionBuilder builder = null;
	    Conjunction pendingConjunction = null;

	    int step = 0; // 0: left, 1: operator, 2: right, 3: conjunction
	    OperandType leftOperandType = null;
	    String leftOperand = null;
	    OperandType rightOperandType = null;
	    String rightOperand = null;
	    IFilterOperator operator = null;

	    for ( int i = 0; i < query.length(); i++ ) {
	        char ch = query.charAt(i);
	        if ( ch == ' ' ) continue;

	        if ( step == 0 || step == 2 ) {
	            OperandType operandType = null;
	            String operand = null;
	            if ( ch == '"' ) {
	                operandType = OperandType.COLUMN;
	                operand = readStringUntil('"', i + 1, query);
	                i += operand.length() + 1;
	            } else if ( ch == '\'' ) {
	                operandType = OperandType.CONST;
	                operand = readStringUntil('\'', i + 1, query);
	                i += operand.length() + 1;
	            }

	            if ( operandType != null ) {
	                if ( step == 0 ) {
	                    leftOperandType = operandType;
	                    leftOperand = operand;
	                } else {
	                    rightOperandType = operandType;
	                    rightOperand = operand;
	                }
	                step++;
	                continue;
	            }
	        }

	        if ( step == 1 ) {
	            char nextCh = (i + 1 < query.length()) ? Character.toUpperCase(query.charAt(i + 1)) : ' ';
	            operator = buildOperator( ch, nextCh );
	            if ( operator == null && ch == 'I' && nextCh == 'S' ) {
	                // Gestione IS NULL / IS NOT NULL
	                String remainder = query.substring(i).toUpperCase();
	                if ( remainder.startsWith("IS NOT NULL") ) {
	                    operator = new IsNotNull();
	                    i += "IS NOT NULL".length() - 1;
	                } else if ( remainder.startsWith("IS NULL") ) {
	                    operator = new IsNull();
	                    i += "IS NULL".length() - 1;
	                }
	                step = 3; // Operatore unario, salta step 2
	                continue;
	            }
	            step++;
	            continue;
	        }

	        if ( step == 3 ) {
	            // Salviamo la condizione corrente
	            IFilterCondition filterCondition = buildFilterCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
	            if ( builder == null ) {
	                builder = new FilterExpressionBuilder().begin( filterCondition );
	            } else {
	                addConjunction( builder, filterCondition, pendingConjunction );
	            }

	            // Ricerca congiunzione successiva
	            pendingConjunction = null;
	            for ( int j = i; j < query.length(); j++ ) {
	                char chj = Character.toUpperCase(query.charAt(j));
	                char chjj = (j + 1 < query.length()) ? Character.toUpperCase(query.charAt(j + 1)) : ' ';
	                if ( chj == 'A' && chjj == 'N' ) {
	                    pendingConjunction = Conjunction.AND;
	                    i = j + 2;
	                    break;
	                } else if ( chj == 'O' && chjj == 'R' ) {
	                    pendingConjunction = Conjunction.OR;
	                    i = j + 1;
	                    break;
	                }
	            }

	            // Reset per la prossima clausola
	            leftOperand = null;
	            leftOperandType = null;
	            rightOperand = null;
	            rightOperandType = null;
	            operator = null;
	            step = 0;
	        }
	    }

	    // Aggiunta dell'ultima clausola
	    IFilterCondition lastCondition = buildFilterCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
	    if ( builder == null ) {
	        builder = new FilterExpressionBuilder().begin( lastCondition );
	    } else {
	        addConjunction( builder, lastCondition, pendingConjunction );
	    }

	    return builder.build();
	}
	
	private static FilterExpressionBuilder addConjunction( FilterExpressionBuilder builder, IFilterCondition filterCondition, Conjunction conjunction ) throws FilterExpressionException {
		switch ( conjunction ) {
		case AND: return builder.and(filterCondition);
		case OR: return builder.or(filterCondition);		
		}
		return builder;
	}
	
	private static IFilterOperator buildOperator( char ch1, char ch2 ) {			
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
	
	private IFilterCondition buildFilterCondition ( 
	        ColumnsModel colsModel,
	        String leftOperand,
	        OperandType leftOperandType,
	        String rightOperand,			
	        OperandType rightOperandType,
	        IFilterOperator operator ) throws RowsTypeException {
	    if ( rightOperand == null && operator instanceof IUnaryOperator ) {
	        return new UnaryCondition(Operand.of(colsModel.getColumn(leftOperand)), (IUnaryOperator)operator);
	    }
	    return buildBinaryCondition(colsModel, leftOperand, leftOperandType, rightOperand, rightOperandType, operator);
	}
	
	private IFilterCondition buildBinaryCondition( 
			ColumnsModel colsModel,
			String leftOperand,
			OperandType leftOperandType,
			String rightOperand,			
			OperandType rightOperandType,
			IFilterOperator operator ) throws RowsTypeException {
		
		if ( OperandType.CONST.equals(rightOperandType) && OperandType.CONST.equals(leftOperandType) ) {
			throw new RuntimeException("Invalid const to const expression");
		}
		Operand left = null;
		Operand right = null;
		
		if ( OperandType.CONST.equals(leftOperandType) ) {
			left = Operand.of(leftOperand, colsModel.getColumn(rightOperand));
			right = Operand.of(colsModel.getColumn(rightOperand));	
		}  else if ( OperandType.CONST.equals(rightOperandType) ) {
			left = Operand.of(colsModel.getColumn(leftOperand));
			right = Operand.of(rightOperand, colsModel.getColumn(leftOperand) );	
		} else {
			left = Operand.of(colsModel.getColumn(leftOperand));
			right = Operand.of(colsModel.getColumn(rightOperand) );	
		}
		return new BinaryCondition( left, (IBinaryOperator)operator, right );
	}
	
	private String readStringUntil(char endChar, int i, String str) {
		return str.substring(i, str.indexOf(endChar, i));
	}
}
