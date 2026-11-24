/*
 * Copyright 2026 Francesco Bianchi
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.blotty.core.parsers;

public enum TokenType {

    COLUMN,

    LITERAL,

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
