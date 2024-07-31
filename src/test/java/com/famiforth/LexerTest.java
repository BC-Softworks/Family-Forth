package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

import com.famiforth.compiler.Lexer;
import com.famiforth.compiler.LexerToken;
import com.famiforth.compiler.LexerToken.TokenType;
import com.famiforth.compiler.LexerUtils;

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
        init(": ;");

        do {
            LexerToken token = lexer.next_token();
            assertEquals(LexerToken.TokenType.KEYWORD, token.type);
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
        LexerToken token = lexer.next_token();
        assertEquals(TokenType.WORD, token.type);
        assertEquals(randString.toUpperCase(), token.value);
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

    @Test
    /**
     * Test Integer to signed hexadecimel conversion
     */
    public void integerToHexTest(){
        assertEquals("0000", LexerUtils.integerToHex(0));
        assertEquals("0008", LexerUtils.integerToHex(8));
        assertEquals("000F", LexerUtils.integerToHex(15));
        assertEquals("0010", LexerUtils.integerToHex(16));
        assertEquals("FFFF", LexerUtils.integerToHex(-1));
        assertEquals("FFF8", LexerUtils.integerToHex(-8));
        assertEquals("FFF1", LexerUtils.integerToHex(-15));
        assertEquals("FFF0", LexerUtils.integerToHex(-16));
    }
}
