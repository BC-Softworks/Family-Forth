package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.famiforth.parser.dictionary.Definition;
import com.famiforth.parser.dictionary.UserDictionary;

public class UserDictionaryTest {
    
    final String testFileName = "src/test/resources/test_dictionary.json";

    UserDictionary dictionary;

    @After public void cleanup() {
        UserDictionary.empty();
    }

    @Test
    public void initalizationTest() throws IOException {
        dictionary = UserDictionary.initalize(testFileName);

        Definition def = UserDictionary.getDefinition("DUP");
        assertEquals(true, def.isPrimitive());
        assertEquals("DUP", def.getName());
        assertEquals("DUP", def.getLabel());
        assertEquals(0, def.getWords().size());
    }

    @Test
    public void specialCharacterTest() throws IOException {
        dictionary = UserDictionary.initalize(testFileName);
        UserDictionary.addWord("?DROP", false, List.of("ZEROEQUALS", "DROP"));
        Definition def = UserDictionary.getDefinition("?DROP");
        assertEquals("?DROP", def.getName());
        assertEquals("qDROP", def.getLabel());
    }

    @Test
    public void getIntegerDefinitionTest() throws IOException {
        dictionary = UserDictionary.initalize(testFileName);
        Definition def = UserDictionary.getDefinition("12345");
        assertEquals("12345", def.getName());
        assertEquals("PUSH #$39, #$30", def.getLabel());
        def = UserDictionary.getDefinition("$1345");
        assertEquals("$1345", def.getName());
        assertEquals("PUSH #$45, #$13", def.getLabel());
    }
}
