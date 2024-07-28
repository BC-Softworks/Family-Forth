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
     * Test the creation of a user defined word made with two primitives
     */
    public void parseColonDefinitionTest() throws IOException{
        init(": D0= 0= SWAP 0= AND ;");
        parser.parse();
        List<List<String>> parsedDefinitions = parser.getParsedDefinitions();
        assertEquals(1, parsedDefinitions.size());
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

        //TODO: Finish test
    }

}
