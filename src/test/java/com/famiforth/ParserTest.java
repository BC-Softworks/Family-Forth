package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.famiforth.compiler.Lexer;
import com.famiforth.compiler.Parser;

public class ParserTest {

    Parser parser;

    private void init(String testString){
        Reader stream = new InputStreamReader(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
        parser = new Parser(new Lexer(stream), "src/test/resources/test_dictionary.json");
    }

    private List<List<String>> parseString() throws IOException {
        List<List<String>> assemblyListOut = new ArrayList<>();
        
        while(parser.hasNext()) {
            assemblyListOut.add(parser.parse());
            System.out.println(assemblyListOut.get(assemblyListOut.size()-1));
        }

        return assemblyListOut;
    }

    @Test
    /**
     * Test parsing integers
     */
    public void parseIntegersTest() throws IOException{
        init("-3 -2 -1 0 1 2 3");
        List<List<String>> assemblyListOut = parseString();
        assertEquals("[[PUSHCELL #FD, #FF], " + 
                    "[PUSHCELL #FE, #FF], " +
                    "[PUSHCELL #FF, #FF], " + 
                    "[PUSHCELL #00, #00], " +
                    "[PUSHCELL #01, #00], "+
                    "[PUSHCELL #02, #00], "+
                    "[PUSHCELL #03, #00]]", assemblyListOut.toString());
        assertEquals(7, assemblyListOut.size());

    }

    @Test
    /**
     * Test the creation of a user defined word made with two primitives
     */
    public void parseColonDefinitionTest() throws IOException{
        init(": D0= 0= SWAP 0= AND ;");
        List<List<String>> assemblyListOut = parseString();
        assertEquals(1, assemblyListOut.size());
        assertEquals(0, assemblyListOut.get(0).size());
    }



    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void parseColonDefinitionWithIntegersTest() throws IOException{
        init(": 2OVER 3 PICK 3 PICK ;");
        List<List<String>> assemblyListOut = parseString();
        assertEquals(1, assemblyListOut.size());
    }

    @Test
    /**
     * Test the creation of a basic word with two primitives and two integers
     */
    public void wordsAndIntegersTest() throws IOException{
        init("1 2 3 SWAP");
        List<List<String>> assemblyListOut = parseString();
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
        List<List<String>> assemblyListOut = parseString();
        
        assertEquals(6, assemblyListOut.size());
        assertEquals("PUSHCELL #03, #00", assemblyListOut.get(1).get(0));
        assertEquals("PUSHCELL #04, #00", assemblyListOut.get(2).get(0));
        assertEquals("PUSHCELL #01, #00", assemblyListOut.get(3).get(0));
        assertEquals("PUSHCELL #02, #00", assemblyListOut.get(4).get(0));
        assertEquals("jsr 2OVER", StringUtils.join(assemblyListOut.get(5), " \n "));

    }
    
}
