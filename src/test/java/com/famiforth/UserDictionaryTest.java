package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.famiforth.dictionary.Definition;
import com.famiforth.dictionary.UserDictionary;

public class UserDictionaryTest {
    
    final String testFileName = "src/test/resources/test_dictionary.json";

    UserDictionary dictionary;

    @Test
    public void initalizationTest() throws IOException {
        dictionary = UserDictionary.initalize(testFileName);
        Definition def = UserDictionary.getDefinition("DROP");
        assertEquals("DROP", def.getName());
        assertEquals(true, def.isPrimitive());
        assertEquals(1, def.getWords().size());
        assertEquals("jsr DROP", def.getAssembly().get(0));

        def = UserDictionary.getDefinition("DUP");
        assertEquals("DUP", def.getName());
        assertEquals(true, def.isPrimitive());
        assertEquals(1, def.getWords().size());
        assertEquals("jsr DUP", def.getAssembly().get(0));
    }

    @Test
    public void specialCharacterTest() throws IOException {
        dictionary = UserDictionary.initalize(testFileName);
        UserDictionary.addWord(Definition.createPrimitiveDefinition("?DROP", List.of("jsr ZEROEQUALS", "jsr DROP")));
        Definition def = UserDictionary.getDefinition("?DROP");
        assertEquals("qDROP", def.getName());
    }
}
