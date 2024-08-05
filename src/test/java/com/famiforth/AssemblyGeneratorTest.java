package com.famiforth;

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
    
    AbstractGenerator generator;

    @Before
    public void setup() throws FileNotFoundException{
        generator = new AssemblyGenerator(new FileOutputStream("build/generator_test.asm"));
    }

    @Test
    public void generateIfStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("IF"), DefinitionType.IF, Pair.of(null));
        generator.generate(token);
    }

    @Test
    public void generateElseStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("ELSE"), DefinitionType.ELSE, Pair.of(null));
        generator.generate(token);
    }

    @Test
    public void generateThenStatementTest() throws IOException {
        setup();
        ParserToken token = new ParserToken(UserDictionary.getAnonymousDefinition("THEN"), DefinitionType.THEN, Pair.of(null));
        generator.generate(token);
    }

}
