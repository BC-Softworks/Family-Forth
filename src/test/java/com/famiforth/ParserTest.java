package com.famiforth;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;


public class ParserTest {

    Parser parser;
    Lexer lexer;
    Reader stream;

    private void init(String testString){
        stream = new InputStreamReader(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
        lexer = new Lexer(stream);
        parser = new Parser(lexer, "src/test/resources/test_dictionary.json", "build/test_out.asm");
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
        List<List<String>> assemblyListOut = parser.getAssemblyListOut();
        assertEquals(7, assemblyListOut.size());
        assertEquals("[[PUSHCELL #FD, #FF], [PUSHCELL #FE, #FF], [PUSHCELL #FF, #FF], [PUSHCELL #00, #00], [PUSHCELL #01, #00], [PUSHCELL #02, #00], [PUSHCELL #03, #00]]", assemblyListOut.toString());
    }

    @Test
    /**
     * Test the creation of a user defined word made with two primitives
     */
    public void parseColonDefinitionTest() throws IOException{
        init(": D0= 0= SWAP 0= AND ;");
        parser.parse();
        List<List<String>> assemblyListOut = parser.getAssemblyListOut();
        assertEquals(1, assemblyListOut.size());
        assertEquals(0, assemblyListOut.get(0).size());
    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void parseColonDefinitionWithIntegersTest() throws IOException{
        init(": 2OVER 3 PICK 3 PICK ;");
        parser.parse();
        List<List<String>> assemblyListOut = parser.getAssemblyListOut();
        assertEquals(1, assemblyListOut.size());
    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void wordsAndIntegersTest() throws IOException{
        init("1 2 3 SWAP");
        parser.parse();
        List<List<String>> assemblyListOut = parser.getAssemblyListOut();
        assertEquals(4, assemblyListOut.size());
        assertEquals("PUSHCELL #01, #00", assemblyListOut.get(0).get(0));
        assertEquals("PUSHCELL #02, #00", assemblyListOut.get(1).get(0));
        assertEquals("PUSHCELL #03, #00", assemblyListOut.get(2).get(0));
        assertEquals("jsr SWAP", assemblyListOut.get(3).get(0));
    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void parseUserDefinedWord() throws IOException{
        init(": 2OVER 3 PICK 3 PICK ; \n 3 4 1 2 2OVER ");
        parser.parse();
        List<List<String>> assemblyListOut = parser.getAssemblyListOut();
        assertEquals(6, assemblyListOut.size());
        assertEquals("PUSHCELL #03, #00", assemblyListOut.get(1).get(0));
        assertEquals("PUSHCELL #04, #00", assemblyListOut.get(2).get(0));
        assertEquals("PUSHCELL #01, #00", assemblyListOut.get(3).get(0));
        assertEquals("PUSHCELL #02, #00", assemblyListOut.get(4).get(0));
        assertEquals("PUSHCELL #03, #00 \n jsr PICK \n PUSHCELL #03, #00 \n jsr PICK", StringUtils.join(assemblyListOut.get(5), " \n "));

    }
    
}
