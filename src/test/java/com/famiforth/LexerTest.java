package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

import com.famiforth.Lexer.Token;
import com.famiforth.Lexer.TokenType;

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
        init(": ; END IF ELSE THEN BEGIN WHILE REPEAT UNTIL DO LOOP +LOOP");

        do {
            Token token = lexer.next_token();
            assertEquals(TokenType.KEYWORD, token.type);
        } while(lexer.hasNext());
    }

    // Test valid user defined words are recognized

    @Test
    /**
     * Test that the lexer capitalizes output
     * This ensure that the created dictionary is not case sensitive
     * @throws IOException
     */
    public void capitalizationTest() throws IOException {
        // Random string of lowercase letters
        String randString = Character.toString(new Random().nextInt(25) + 'a').repeat(java.lang.Math.abs(new Random().nextInt() % 64));

        init(randString);
        Token token = lexer.next_token();
        assertEquals(TokenType.WORD, token.type);
        assertEquals(randString.toUpperCase(), token.value);
    }

    // Test numeric input

    @Test
    public void signedNumberTest() throws IOException {
        String minShort = "" + Short.MIN_VALUE;
        init(minShort);
        Token token = lexer.next_token();
        assertEquals(TokenType.INTEGER, token.type);
        assertEquals(minShort, token.value);
    }

    @Test
    public void floatingPointNumberTest() throws IOException {
        String pi = Double.valueOf(Math.PI).toString();
        init(pi);
        Token token = lexer.next_token();
        assertEquals(TokenType.FLOAT, token.type);
        assertEquals(pi, token.value);
    }


    @Test
    public void ignoreCommentsTest() throws IOException {
        init("( addr -- x )");
        Token token = lexer.next_token();
        assertEquals(TokenType.BEGIN_COMMENT, token.type);
        assertEquals("(", token.value);
        lexer.next_token();
        lexer.next_token();
        lexer.next_token();
        token = lexer.next_token();
        assertEquals(TokenType.END_COMMENT, token.type);
        assertEquals(")", token.value);
    }
}
