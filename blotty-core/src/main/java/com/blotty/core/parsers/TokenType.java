private enum TokenType {

    COLUMN,
    CONST,

    NUMBER,
    BOOLEAN,
    NULL,

    EQ,
    NE,
    GT,
    GTE,
    LT,
    LTE,
    LIKE,

    IS_NULL,
    IS_NOT_NULL,

    AND,
    OR,
    NOT,

    LPAREN,
    RPAREN,

    EOF
}
