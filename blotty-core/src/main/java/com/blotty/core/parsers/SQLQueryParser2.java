package com.blotty.core.parsers;

import java.util.ArrayList;
import java.util.List;

import com.blotty.core.commons.exceptions.FilterExpressionException;
import com.blotty.core.commons.exceptions.RowsTypeException;
import com.blotty.core.models.Column;
import com.blotty.core.models.ColumnsModel;
import com.blotty.core.modifiers.filters.FilterExpression;
import com.blotty.core.modifiers.filters.IFilterCondition;
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
import com.blotty.core.modifiers.filters.conditions.logical.AndCondition;
import com.blotty.core.modifiers.filters.conditions.logical.NotCondition;
import com.blotty.core.modifiers.filters.conditions.logical.OrCondition;
import com.blotty.core.modifiers.filters.conditions.unary.UnaryCondition;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNotNull;
import com.blotty.core.modifiers.filters.conditions.unary.operators.IsNull;

public class SQLQueryParser2 {

    private ColumnsModel columns;
    private List<Token> tokens;
    private int pos;

    public FilterExpression parse(
            ColumnsModel columns,
            String query)
            throws FilterExpressionException, RowsTypeException {

        this.columns = columns;
        this.tokens = tokenize(query);
        this.pos = 0;
        IFilterCondition root = parseOr();
        expect(TokenType.EOF);
        return new FilterExpression(root);
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
            return new NotCondition(parsePrimary());
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
            return new UnaryCondition(
                    Operand.of(leftColumn),
                    new IsNull());
        }

        if (match(TokenType.IS_NOT_NULL)) {
            return new UnaryCondition(
                    Operand.of(leftColumn),
                    new IsNotNull());
        }

        Token operatorToken = consume();
        Token rightToken = consume();
        Operand left = Operand.of(leftColumn);
        Operand right;

        if (rightToken.type() == TokenType.COLUMN) {
            right = Operand.of( getColumn(rightToken.value()));
        } else if (rightToken.type() == TokenType.CONST) {
            right = Operand.of( rightToken.value(), leftColumn);
        } else {
            throw error("Expected column or constant");
        }
        return new BinaryCondition(
                left,
                toOperator(operatorToken),
                right);
    }

    //------------------------------------------------------------
    // Tokenizer
    //------------------------------------------------------------
    private List<Token> tokenize(String query) throws FilterExpressionException {
        List<Token> result = new ArrayList<>();
        int i = 0;
        while (i < query.length()) {
            char c = query.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '(') { result.add( new Token(TokenType.LPAREN,"("));
                i++;
                continue;
            }
            if (c == ')') {
                result.add( new Token(TokenType.RPAREN, ")"));
                i++;
                continue;
            }
            if (c == '"') {
                int end = query.indexOf('"', i + 1);
                if (end < 0) {
                    throw error("Missing closing quote");
                }
                result.add( new Token(
                                TokenType.COLUMN,
                                query.substring( i + 1, end)));
                i = end + 1;
                continue;
            }

            if (c == '\'') {
                int end = query.indexOf('\'', i + 1);
                if (end < 0) {
                    throw error("Missing closing quote");
                }

                result.add( new Token(
                                TokenType.CONST,
                                query.substring(i + 1, end)));
                i = end + 1;
                continue;
            }

            if (Character.isDigit(c) || c == '-') {
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
            
                String value = query.substring(start, i);
            
                result.add(
                    new Token(
                        dotFound
                            ? TokenType.DOUBLE
                            : TokenType.INTEGER,
                        value
                    )
                );            
                continue;
            }

            String rem = query.substring(i).toUpperCase();

            if (rem.startsWith("IS NOT NULL")) {
                result.add( new Token(
                                TokenType.IS_NOT_NULL,
                                "IS NOT NULL"));
                i += 11;
                continue;
            }

            if (rem.startsWith("IS NULL")) {
                result.add(
                        new Token(
                                TokenType.IS_NULL,
                                "IS NULL"));
                i += 7;
                continue;
            }

            if (rem.startsWith("LIKE")) {
                result.add(
                        new Token(
                                TokenType.LIKE,
                                "LIKE"));
                i += 4;
                continue;
            }

            if (rem.startsWith("AND")) {
                result.add(
                        new Token(
                                TokenType.AND,
                                "AND"));
                i += 3;
                continue;
            }

            if (rem.startsWith("OR")) {
                result.add(
                        new Token(
                                TokenType.OR,
                                "OR"));
                i += 2;
                continue;
            }

            if (rem.startsWith("NOT")) {
                result.add(
                        new Token(
                                TokenType.NOT,
                                "NOT"));
                i += 3;
                continue;
            }
            if (rem.startsWith("TRUE")) {
                result.add(
                    new Token(
                        TokenType.BOOLEAN,
                        "true"));
            
                i += 4;
                continue;
            }
            
            if (rem.startsWith("FALSE")) {          
                result.add(
                    new Token(
                        TokenType.BOOLEAN,
                        "false"));
            
                i += 5;
                continue;
            }

            if (rem.startsWith("NULL")) {
                result.add(
                    new Token(
                        TokenType.NULL,
                        "NULL"));
            
                i += 4;
                continue;
            }
            
            if (rem.startsWith(">=")) {
                result.add(
                        new Token(
                                TokenType.GTE,
                                ">="));
                i += 2;
                continue;
            }

            if (rem.startsWith("<=")) {
                result.add(
                        new Token(
                                TokenType.LTE,
                                "<="));
                i += 2;
                continue;
            }

            if (rem.startsWith("!=")) {
                result.add(
                        new Token(
                                TokenType.NE,
                                "!="));
                i += 2;
                continue;
            }

            if (rem.startsWith("=")) {
                result.add(
                        new Token(
                                TokenType.EQ,
                                "="));
                i++;
                continue;
            }

            if (rem.startsWith(">")) {
                result.add(
                        new Token(
                                TokenType.GT,
                                ">"));
                i++;
                continue;
            }

            if (rem.startsWith("<")) {
                result.add(
                        new Token(
                                TokenType.LT,
                                "<"));
                i++;
                continue;
            }

            throw error(
                    "Unexpected token near: "
                    + rem);
        }

        result.add(
                new Token(
                        TokenType.EOF,
                        ""));

        return result;
    }

    //------------------------------------------------------------
    // Utility
    //------------------------------------------------------------

    private IBinaryOperator toOperator(
            Token token) {

        return switch (token.type()) {

            case EQ -> new Equal();

            case NE -> new NotEqual();

            case GT -> new GreaterThan();

            case GTE -> new GreaterThanEqual();

            case LT -> new LesserThan();

            case LTE -> new LesserThanEqual();

            case LIKE -> new Like();

            default ->
                throw new IllegalArgumentException(
                        "Unsupported operator: "
                        + token.type());
        };
    }

    private Column getColumn(String name) {

        Column c = columns.getColumn(name);

        if (c == null) {
            throw new IllegalArgumentException(
                    "Unknown column: " + name);
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

    private Token expect(TokenType type)
            throws FilterExpressionException {

        Token token = consume();

        if (token.type() != type) {

            throw error(
                    "Expected "
                    + type
                    + " but found "
                    + token.type());
        }

        return token;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private FilterExpressionException error(
            String msg) {

        return new FilterExpressionException(msg);
    }
}
