package com.famiforth.compiler;

// Define Token and token types
public class LexerToken {
    public String value;
    public TokenType type;
    protected int lineNumber;
    protected int tokenNumber;

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
        INTEGER,
        WORD
    }
}