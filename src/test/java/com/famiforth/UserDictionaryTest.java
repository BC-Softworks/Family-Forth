package com.famiforth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

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
    
}
