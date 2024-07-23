package com.famiforth;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

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
    public void parseColonDefinitionTest() throws IOException{
        init(": 2DROP DROP DROP ;");
        parser.parse();
        //TODO: Finish test
    }

    @Test
    public void parseColonDefinitionWithIntegersTest() throws IOException{
        init(": 2OVER 3 PICK 3 PICK  ;");
        parser.parse();
        //TODO: Finish test
    }

}
