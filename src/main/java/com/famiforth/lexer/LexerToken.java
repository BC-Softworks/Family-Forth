package com.famiforth.lexer;

// Define Token and token types
public class LexerToken {
    public String value;
    public TokenType type;
    public int lineNumber;
    public int tokenNumber;

    public LexerToken(String value, TokenType type, int lineNumber, int tokenNumber) {
        this.value = value;
        this.type = type;
        this.lineNumber = lineNumber;
        this.tokenNumber = tokenNumber;
    }

    public enum TokenType{
        SKIP_LINE,
        BEGIN_COMMENT,
        END_COMMENT,
        KEYWORD,
        FLOAT,
        HEX,
        DECIMAL,
        WORD
    }
}