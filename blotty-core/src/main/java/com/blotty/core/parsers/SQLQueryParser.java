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
import com.blotty.core.models.columns.Column;
import com.blotty.core.models.columns.ColumnsModel;
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

/**
 * SQL-inspired filter parser used by Blotty.
 *
 * The parser converts a textual filter expression into an Abstract
 * Syntax Tree (AST) composed of:
 *
 * - BinaryCondition
 * - UnaryCondition
 * - AndCondition
 * - OrCondition
 * - NotCondition
 *
 * Example:
 *
 *      "STATUS" = 'OPEN'
 *      AND
 *      "QTY" > 100
 *
 * becomes:
 *
 *              AND
 *             /   \
 *            EQ    GT
 *
 * The parser is implemented as a simple recursive-descent parser with
 * operator precedence handled by the grammar hierarchy:
 *
 *      NOT > AND > OR
 *
 * Parenthesized expressions are fully supported.
 */
public class SQLQueryParser {

	private ColumnsModel columns;
	private List<Token> tokens;
	private int pos;

	/**
	 * Entry point.
	 *
	 * Parsing is performed in two phases:
	 *
	 * 1. Tokenization
	 *      String -> List<Token>
	 *
	 * 2. AST construction
	 *      List<Token> -> IFilterCondition tree
	 *
	 * After parsing, the root node is wrapped inside a FilterExpression
	 * and stored inside a QueryDefinition.
	 */
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
	// The grammar is intentionally organized to enforce operator
	// precedence.
	//
	//      NOT  (highest)
	//      AND
	//      OR   (lowest)
	//
	// Example:
	//
	//      A OR B AND C
	//
	// is parsed as:
	//
	//      A OR (B AND C)
	//
	// and not:
	//
	//      (A OR B) AND C
	//
	// Parentheses can always be used to explicitly override
	// precedence.
	//
	// Example:
	//
	//      (A OR B) AND C
	//------------------------------------------------------------

	/**
	 * Parses OR expressions.
	 *
	 * OR is the lowest precedence operator of the language,
	 * therefore it is the top-level rule of the grammar.
	 *
	 * Example:
	 *
	 *      A OR B OR C
	 *
	 * generates:
	 *
	 *             OR
	 *            /  \
	 *          OR    C
	 *         /  \
	 *        A    B
	 */
	private IFilterCondition parseOr() throws FilterExpressionException, RowsTypeException {
		IFilterCondition left = parseAnd();
		while (match(TokenType.OR)) {
			left = new OrCondition( left, parseAnd());
		}
		return left;
	}

	/**
	 * Parses AND expressions.
	 *
	 * AND has higher precedence than OR.
	 *
	 * Example:
	 *
	 *      A AND B OR C
	 *
	 * becomes:
	 *
	 *          OR
	 *         /  \
	 *       AND   C
	 *       / \
	 *      A   B
	 */
	private IFilterCondition parseAnd() throws FilterExpressionException, RowsTypeException {
		IFilterCondition left = parseNot();
		while (match(TokenType.AND)) {
			left = new AndCondition( left, parseNot());
		}
		return left;
	}

	/**
	 * Parses unary NOT expressions.
	 *
	 * NOT is right-associative and has the highest precedence
	 * among logical operators.
	 *
	 * Example:
	 *
	 *      NOT A
	 *
	 * becomes:
	 *
	 *          NOT
	 *           |
	 *           A
	 *
	 * Example:
	 *
	 *      NOT NOT A
	 *
	 * becomes:
	 *
	 *          NOT
	 *           |
	 *          NOT
	 *           |
	 *           A
	 */
	private IFilterCondition parseNot() throws FilterExpressionException, RowsTypeException {
		if (match(TokenType.NOT)) {        
			if (peek().type() == TokenType.EOF) {
				throw error("Expected expression after NOT");
			}        
			return new NotCondition(parseNot());
		}
		return parsePrimary();
	}

	/**
	 * Parses the smallest evaluable unit.
	 *
	 * A primary expression can be:
	 *
	 * - a parenthesized sub-expression
	 * - a leaf condition
	 *
	 * Parentheses cause a recursive descent back into the OR
	 * rule, effectively creating a nested AST subtree.
	 */
	private IFilterCondition parsePrimary() throws FilterExpressionException, RowsTypeException {
		if (match(TokenType.LPAREN)) {
			IFilterCondition expr = parseOr();
			expect(TokenType.RPAREN);
			return expr;
		}
		return parseCondition();
	}

	/**
	 * Parses a filter leaf node.
	 *
	 * Supported forms:
	 *
	 *      "COL" = 'value'
	 *      "COL" > 10
	 *      "COL" LIKE 'ABC'
	 *      "COL" IS NULL
	 *      "COL" IS NOT NULL
	 *      "COL1" = "COL2"
	 *
	 * Column-to-column comparisons are resolved during execution,
	 * not during parsing.
	 */
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

	/**
	 * Validates that the current token is a supported
	 * binary comparison operator.
	 *
	 * Unary operators (IS NULL / IS NOT NULL) are handled
	 * separately inside parseCondition().
	 */
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
	/**
	 * Converts the input query string into a flat token stream.
	 *
	 * The tokenizer performs no semantic validation and does not
	 * build any AST structures.
	 *
	 * Example:
	 *
	 *      "AGE" >= 18 AND "ACTIVE" = true
	 *
	 * becomes:
	 *
	 *      COLUMN(AGE)
	 *      GTE
	 *      LITERAL(18)
	 *      AND
	 *      COLUMN(ACTIVE)
	 *      EQ
	 *      LITERAL(true)
	 *
	 * Parsing and operator precedence are handled later by
	 * the recursive-descent parser.
	 */
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

	/**
	 * Ensures that a keyword is recognized only when
	 * delimited by a token boundary.
	 *
	 * Example:
	 *
	 *      AND      -> valid keyword
	 *      ANDREA   -> not a keyword
	 *
	 * This prevents accidental matches inside identifiers.
	 */
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

	/**
	 * Maps parser tokens to executable operator objects.
	 *
	 * This is the bridge between the parser layer and the
	 * filter execution engine.
	 */
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

	/**
	 * Consumes the next token only if it matches the expected type.
	 *
	 * Unlike expect(), no exception is thrown when the match fails.
	 */
	private boolean match(TokenType type) {
		if (peek().type() == type) {
			pos++;
			return true;
		}
		return false;
	}

	/**
	 * Consumes the next token and verifies its type.
	 *
	 * A FilterExpressionException is thrown whenever the expected
	 * grammar rule cannot be satisfied.
	 */
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
	/**
	 * Returns the next token without consuming it.
	 */
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
