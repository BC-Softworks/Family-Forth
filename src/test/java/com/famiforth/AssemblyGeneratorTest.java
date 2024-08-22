package com.famiforth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.famiforth.generator.AbstractGenerator;
import com.famiforth.generator.AssemblyGenerator;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.ParserToken.DefinitionType;
import com.famiforth.parser.dictionary.UserDictionary;

public class AssemblyGeneratorTest {
    
    private AbstractGenerator generator;

    private FileOutputStream fileOut;

    @Before
    public void setup() throws FileNotFoundException{
        fileOut = mock(FileOutputStream.class);
        generator = new AssemblyGenerator(fileOut);
    }

    @Test
    public void generateIfStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("IF"), DefinitionType.IF, Pair.of("0",""));
        assertEquals("IF 0", generator.generate(token).get(0));
    }

    @Test
    public void generateElseStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("ELSE"), DefinitionType.ELSE, Pair.of("0","1"));
        generator.generate(token);
        assertEquals("clc", generator.generate(token).get(0));
        assertEquals("bcc 1", generator.generate(token).get(1));
        assertEquals("0: ELSE", generator.generate(token).get(2));
    }

    @Test
    public void generateThenStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("THEN"), DefinitionType.THEN, Pair.of("0", ""));
        generator.generate(token);
        assertEquals("0: THEN", generator.generate(token).get(0));
    }

    @Test
    public void generateRecurseStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("RECURSE"), DefinitionType.RECURSE, Pair.of("", ""), "NAME");
        generator.generate(token);
        assertEquals("RECURSE NAME", generator.generate(token).get(0));
    }
}
