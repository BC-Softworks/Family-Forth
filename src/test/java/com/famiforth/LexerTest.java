package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.famiforth.lexer.Lexer;
import com.famiforth.lexer.LexerToken;
import com.famiforth.lexer.LexerToken.TokenType;

public class LexerTest {

    Lexer lexer;
    Reader stream;

    private void init(String testString){
        stream = new InputStreamReader(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
        lexer = new Lexer(stream);
    }

    // Test keywords

    @Test
    public void keywordTest() throws IOException {
        init(": ; IF ELSE THEN");

        for(int i = 0; i < 5; i++) {
            LexerToken token = lexer.next_token();
            assertEquals(LexerToken.TokenType.KEYWORD, token.type);
        }
    }

    // Test numeric input

    @Test
    public void signedNumberTest() throws IOException {
        String minShort = "" + Short.MIN_VALUE;
        init(minShort);
        LexerToken token = lexer.next_token();
        assertEquals(TokenType.INTEGER, token.type);
        assertEquals(minShort, token.value);
    }

    @Test
    public void floatingPointNumberTest() throws IOException {
        String pi = Double.valueOf(Math.PI).toString();
        init(pi);
        LexerToken token = lexer.next_token();
        assertEquals(TokenType.FLOAT, token.type);
        assertEquals(pi, token.value);
    }


    @Test
    public void ignoreCommentsTest() throws IOException {
        init("( addr -- x )");
        LexerToken token = lexer.next_token();
        assertEquals(TokenType.BEGIN_COMMENT, token.type);
        assertEquals("(", token.value);
        lexer.next_token();
        lexer.next_token();
        lexer.next_token();
        token = lexer.next_token();
        assertEquals(TokenType.END_COMMENT, token.type);
        assertEquals(")", token.value);
    }

    @Test
    public void skipLineTest() throws IOException {
        init("\\");
        LexerToken token = lexer.next_token();
        assertEquals(TokenType.SKIP_LINE, token.type);
        assertEquals("\\", token.value);
    }

}
