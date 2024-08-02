package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.famiforth.lexer.Lexer;
import com.famiforth.parser.Parser;
import com.famiforth.parser.ParserToken;

public class ParserTest {

    Parser parser;

    private void init(String testString) {
        Reader stream = new InputStreamReader(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
        parser = new Parser(new Lexer(stream), "src/test/resources/test_dictionary.json");
    }

    private List<ParserToken> parseString() throws IOException {
        List<ParserToken> assemblyListOut = new ArrayList<>();

        while (parser.hasNext()) {
            assemblyListOut.add(parser.parse());
        }

        return assemblyListOut;
    }

    @Test
    /**
     * Test parsing integers
     */
    public void parseIntegersTest() throws IOException {
        init("-3 -2 -1 0 1 2 3");
        List<ParserToken> tokens = parseString();
        assertEquals(7, tokens.size());
        assertEquals("Definition [-3: []]", tokens.get(0).def.toString());
        assertEquals("Definition [-2: []]", tokens.get(1).def.toString());
        assertEquals("Definition [-1: []]", tokens.get(2).def.toString());
        assertEquals("Definition [0: []]", tokens.get(3).def.toString());
        assertEquals("Definition [1: []]", tokens.get(4).def.toString());
        assertEquals("Definition [2: []]", tokens.get(5).def.toString());
        assertEquals("Definition [3: []]", tokens.get(6).def.toString());

    }

    @Test
    /**
     * Test the creation of a user defined word made with two primitives
     */
    public void parseColonDefinitionTest() throws IOException {
        init(": D0= 0= SWAP 0= AND ;");
        List<ParserToken> tokensList = parseString();
        assertEquals(1, tokensList.size());
        assertEquals("Definition [D0=: [0=, SWAP, 0=, AND]]", tokensList.get(0).def.toString());

    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void parseColonDefinitionWithIntegersTest() throws IOException {
        init(": 2OVER 3 PICK 3 PICK ;");
        List<ParserToken> tokensList = parseString();
        assertEquals(1, tokensList.size());
        assertEquals("Definition [2OVER: [3, PICK, 3, PICK]]", tokensList.get(0).def.toString());

    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void wordsAndIntegersTest() throws IOException {
        init("1 2 3 SWAP");
        List<ParserToken> tokensList = parseString();
        assertEquals(4, tokensList.size());
    }
}
