package com.famiforth;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;


public class ParserTest {

    Parser parser;
    Lexer lexer;
    Reader stream;

    private void init(String testString){
        stream = new InputStreamReader(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
        lexer = new Lexer(stream);
        parser = new Parser(lexer, "src/test/resources/test_dictionary.json");
    }

    @Test
    /**
     * Test Integer to signed hexadecimel conversion
     */
    public void integerToHexTest(){
        assertEquals("0000", ParserUtils.integerToHex(0));
        assertEquals("0008", ParserUtils.integerToHex(8));
        assertEquals("000F", ParserUtils.integerToHex(15));
        assertEquals("0010", ParserUtils.integerToHex(16));
        assertEquals("FFFF", ParserUtils.integerToHex(-1));
        assertEquals("FFF8", ParserUtils.integerToHex(-8));
        assertEquals("FFF1", ParserUtils.integerToHex(-15));
        assertEquals("FFF0", ParserUtils.integerToHex(-16));
    }

    @Test
    /**
     * Test parsing integers
     */
    public void parseIntegersTest() throws IOException{
        init("-3 -2 -1 0 1 2 3");
        parser.parse();
        List<List<String>> parsedDefinitions = parser.getParsedDefinitions();
        assertEquals(7, parsedDefinitions.size());
        assertEquals("[[PUSH #FD, #FF], [PUSH #FE, #FF], [PUSH #FF, #FF], [PUSH #00, #00], [PUSH #01, #00], [PUSH #02, #00], [PUSH #03, #00]]", parsedDefinitions.toString());
    }

    @Test
    /**
     * Test the creation of a user defined word made with two primitives
     */
    public void parseColonDefinitionTest() throws IOException{
        init(": D0= 0= SWAP 0= AND ;");
        parser.parse();
        List<List<String>> parsedDefinitions = parser.getParsedDefinitions();
        assertEquals(1, parsedDefinitions.size());
        assertEquals("[0=, SWAP, 0=, AND]", parsedDefinitions.get(0).toString());
    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void parseColonDefinitionWithIntegersTest() throws IOException{
        init(": 2OVER 3 PICK 3 PICK ;");
        parser.parse();
        List<List<String>> parsedDefinitions = parser.getParsedDefinitions();
        assertEquals(1, parsedDefinitions.size());
    }
    

}
