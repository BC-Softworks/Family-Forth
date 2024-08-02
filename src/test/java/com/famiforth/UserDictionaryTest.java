package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.famiforth.parser.dictionary.Definition;
import com.famiforth.parser.dictionary.UserDictionary;

public class UserDictionaryTest {
    
    final String testFileName = "src/test/resources/test_dictionary.json";

    UserDictionary dictionary;

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
        UserDictionary.addUserDefinedWord("?DROP", false, List.of("ZEROEQUALS", "DROP"));
        Definition def = UserDictionary.getDefinition("?DROP");
        assertEquals("?DROP", def.getName());
        assertEquals("qDROP", def.getLabel());
    }
}
