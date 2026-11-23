/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.parsers;

import java.util.ArrayList;
import java.util.List;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.models.query.QueryDefinition;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.conditions.AndCondition;
import com.blotty.core.modifiers.filters.conditions.IFilterCondition;
import com.blotty.core.modifiers.filters.conditions.NotCondition;
import com.blotty.core.modifiers.filters.conditions.Operand;
import com.blotty.core.modifiers.filters.conditions.OrCondition;
import com.blotty.core.modifiers.filters.conditions.binary.BinaryCondition;
import com.blotty.core.modifiers.filters.conditions.binary.IBinaryOperator;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Equal;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.GreaterThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThan;
import com.blotty.core.modifiers.filters.conditions.binary.operators.LesserThanEqual;
import com.blotty.core.modifiers.filters.conditions.binary.operators.Like;
import com.blotty.core.modifiers.filters.conditions.binary.operators.NotEqual;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;

public class SQLQueryParser {

	private ColumnsModel columns;
	private List<Token> tokens;
	private int pos;

	public QueryDefinition parse(ColumnsModel columns,	String query)throws FilterExpressionException, RowsTypeException {
		this.columns = columns;
		this.tokens = tokenize(query);
		this.pos = 0;
		IFilterCondition root = parseOr();
		expect(TokenType.EOF);
		return new QueryDefinition( new FilterExpression(root), null /* impl sorters */ );
	}

	//------------------------------------------------------------
	// Grammar
	//
	// expression   := or
	// or           := and ( OR and )*
	// and          := not ( AND not )*
	// not          := NOT primary | primary
	// primary      := '(' or ')' | condition
	// condition    := ...
	//------------------------------------------------------------
	private IFilterCondition parseOr() throws FilterExpressionException, RowsTypeException {
		IFilterCondition left = parseAnd();
		while (match(TokenType.OR)) {
			left = new OrCondition( left, parseAnd());
		}
		return left;
	}

	private IFilterCondition parseAnd() throws FilterExpressionException, RowsTypeException {
		IFilterCondition left = parseNot();
		while (match(TokenType.AND)) {
			left = new AndCondition( left, parseNot());
		}
		return left;
	}

	private IFilterCondition parseNot() throws FilterExpressionException, RowsTypeException {
		if (match(TokenType.NOT)) {        
			if (peek().type() == TokenType.EOF) {
				throw error("Expected expression after NOT");
			}        
			return new NotCondition(parseNot());
		}
		return parsePrimary();
	}

	private IFilterCondition parsePrimary() throws FilterExpressionException, RowsTypeException {
		if (match(TokenType.LPAREN)) {
			IFilterCondition expr = parseOr();
			expect(TokenType.RPAREN);
			return expr;
		}
		return parseCondition();
	}

	private IFilterCondition parseCondition() throws FilterExpressionException, RowsTypeException {
		Token leftToken = expect(TokenType.COLUMN);
		Column leftColumn = getColumn(leftToken.value());
		if (match(TokenType.IS_NULL)) {
			return new UnaryCondition(Operand.of(leftColumn),new IsNull());
		}

		if (match(TokenType.IS_NOT_NULL)) {
			return new UnaryCondition(Operand.of(leftColumn),new IsNotNull());
		}

		Token operatorToken = expectOperator();
		Token rightToken = consume();

		if (rightToken.type() != TokenType.COLUMN && rightToken.type() != TokenType.LITERAL) {
			throw error("Expected COLUMN or LITERAL but found " + rightToken.type());
		}

		Operand left = Operand.of(leftColumn);
		Operand right;

		switch (rightToken.type()) {
		case COLUMN:
			right = Operand.of(getColumn(rightToken.value()));
			break;            
		case LITERAL:            
			if ("NULL".equalsIgnoreCase(rightToken.value())) {
				throw error("Use IS NULL or IS NOT NULL instead of "
								+ operatorToken.value()
								+ " NULL");
			}            
			right = Operand.of(rightToken.value(), leftColumn);            
			break;        
		default:
			throw error("Expected COLUMN or LITERAL, found "+ rightToken.type());
		}      
		return new BinaryCondition(
				left,
				toOperator(operatorToken),
				right);
	}

	private Token expectOperator() throws FilterExpressionException {
		Token token = consume();   
		switch (token.type()) {
		case EQ:
		case NE:
		case GT:
		case GTE:
		case LT:
		case LTE:
		case LIKE:
			return token;    
		default:
			throw error("Expected operator but found "+ token.type());
		}
	}

	//------------------------------------------------------------
	// Tokenizer
	//------------------------------------------------------------
	private List<Token> tokenize(String query) throws FilterExpressionException {
		List<Token> result = new ArrayList<>();
		int i = 0;
		while (i < query.length()) {
			char c = query.charAt(i);

			// whitespace
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			// (
			if (c == '(') {
				result.add(new Token(TokenType.LPAREN,"("));
				i++;
				continue;
			}
			// )
			if (c == ')') {
				result.add(	new Token(TokenType.RPAREN,")"));
				i++;
				continue;
			}
			// "COLUMN"
			if (c == '"') {
				int end = query.indexOf('"', i + 1);
				if (end < 0) {
					throw new FilterExpressionException(
							"Missing closing double quote");
				}
				result.add(	new Token(TokenType.COLUMN, query.substring(i + 1, end)));
				i = end + 1;
				continue;
			}
			// 'STRING LITERAL'
			if (c == '\'') {
				int end = query.indexOf('\'', i + 1);

				if (end < 0) {
					throw new FilterExpressionException(
							"Missing closing single quote");
				}
				result.add(	new Token(TokenType.LITERAL, query.substring(i + 1, end)));
				i = end + 1;
				continue;
			}
			// numeric literals
			if (Character.isDigit(c) ||	(c == '-' && i + 1 < query.length()	&& Character.isDigit(query.charAt(i+1)))) {
				int start = i;
				boolean dotFound = false;
				i++;

				while (i < query.length()) {
					char n = query.charAt(i);
					if (Character.isDigit(n)) {
						i++;
						continue;
					}
					if (n == '.') {
						if (dotFound) {
							break;
						}
						dotFound = true;
						i++;
						continue;
					}
					break;
				}
				result.add(	new Token(TokenType.LITERAL,query.substring(start, i)));
				continue;
			}

			// keywords
			String rem = query.substring(i);
			String upper = rem.toUpperCase();

			if (startsWithKeyword(upper, "IS NOT NULL")){
				result.add(	new Token(TokenType.IS_NOT_NULL,"IS NOT NULL"));
				i += "IS NOT NULL".length();
				continue;
			}
			if (startsWithKeyword(upper, "IS NULL")){
				result.add(new Token(TokenType.IS_NULL,"IS NULL"));
				i += "IS NULL".length();
				continue;
			}
			if (startsWithKeyword(upper, "LIKE")){
				result.add(	new Token(TokenType.LIKE,"LIKE"));
				i += 4;
				continue;
			}
			if (startsWithKeyword(upper, "AND")){
				result.add(	new Token(TokenType.AND,"AND"));
				i += 3;
				continue;
			}
			if (startsWithKeyword(upper, "OR")){    
				result.add(new Token(TokenType.OR,"OR"));
				i += 2;
				continue;
			}
			if (startsWithKeyword(upper, "NOT")){ 
				result.add(	new Token(TokenType.NOT,"NOT"));
				i += 3;
				continue;
			}
			if (startsWithKeyword(upper, "TRUE")){
				result.add(	new Token(TokenType.LITERAL,"true"));
				i += 4;
				continue;
			}
			if (startsWithKeyword(upper, "FALSE")){    
				result.add(	new Token(TokenType.LITERAL,"false"));
				i += 5;
				continue;
			}
			if (startsWithKeyword(upper, "NULL")){    
				result.add(new Token(TokenType.LITERAL,	"NULL"));
				i += 4;
				continue;
			}

			//--------------------------------------
			// operators
			//--------------------------------------
			if (upper.startsWith(">=")) {
				result.add(	new Token(TokenType.GTE,">="));
				i += 2;
				continue;
			}
			if (upper.startsWith("<=")) {
				result.add(	new Token(TokenType.LTE,"<="));
				i += 2;
				continue;
			}
			if (upper.startsWith("!=")) {
				result.add(	new Token(TokenType.NE,"!="));
				i += 2;
				continue;
			}
			if (upper.startsWith("=")) {
				result.add(	new Token(TokenType.EQ,"="));
				i++;
				continue;
			}
			if (upper.startsWith(">")) {
				result.add(	new Token(TokenType.GT,">"));
				i++;
				continue;
			}

			if (upper.startsWith("<")) {
				result.add(	new Token(TokenType.LT,"<"));
				i++;
				continue;
			}

			throw new FilterExpressionException(
					"Unexpected token near: " + rem);
		}

		result.add(	new Token(TokenType.EOF,""));
		return result;
	}

	private static boolean startsWithKeyword( String text, String keyword) {
		if (!text.startsWith(keyword)) {
			return false;
		}    
		if (text.length() == keyword.length()) {
			return true;
		}    
		char next = text.charAt(keyword.length());
		return Character.isWhitespace(next)
				|| next == '('
				|| next == ')';
	}

	//------------------------------------------------------------
	// Utility
	//------------------------------------------------------------

	private static IBinaryOperator toOperator(Token token) {
		return switch (token.type()) {
		case EQ -> new Equal();
		case NE -> new NotEqual();
		case GT -> new GreaterThan();
		case GTE -> new GreaterThanEqual();
		case LT -> new LesserThan();
		case LTE -> new LesserThanEqual();
		case LIKE -> new Like();
		default ->
			throw new IllegalArgumentException("Unsupported operator: "+ token.type());
		};
	}

	private Column getColumn(String name) {
		Column c = columns.getColumn(name);
		if (c == null) {
			throw new IllegalArgumentException("Unknown column: " + name);
		}
		return c;
	}

	private boolean match(TokenType type) {
		if (peek().type() == type) {
			pos++;
			return true;
		}
		return false;
	}

	private Token expect(TokenType type) throws FilterExpressionException {
		Token token = consume();   
		if (token.type() != type) {    
			throw error(
					"Parse error. Expected "
							+ type
							+ " but found "
							+ token.type()
							+ " at token position "
							+ (pos - 1));
		}    
		return token;
	}

	private Token peek() {    
		if (pos >= tokens.size()) {
			return new Token(TokenType.EOF, "");
		}    
		return tokens.get(pos);
	}

	private Token consume() throws FilterExpressionException {    
		if (pos >= tokens.size()) {
			throw error("Unexpected end of query");
		}    
		return tokens.get(pos++);
	}

	private FilterExpressionException error( String msg) {
		return new FilterExpressionException(msg);
	}
}
