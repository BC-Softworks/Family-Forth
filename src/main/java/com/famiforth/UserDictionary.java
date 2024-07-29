package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

public class UserDictionary {
    private static boolean initalized = false;

    private static UserDictionary instance = null;
    private static Dictionary<String, Definition> dictionary;

    // Prevent the creation of an empty dictionary
    private UserDictionary() {
    }

    private UserDictionary(String fileName) throws IOException {
        dictionary = new Hashtable<String, Definition>();
        populateDictionary(fileName);
    }

    /**
     * Create instance of singleton dictionary
     * 
     * @param fileName
     * @return UserDictionary instance
     */
    public static UserDictionary initalize(String fileName) {
        if (instance == null) {
            try {
                instance = new UserDictionary(fileName);
            } catch (IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
            initalized = true;
        }
        return instance;
    }

    /**
     * Populate the current dictionary instance with the
     * contents of the provided JSON files. If a word is encountered twice,
     * a warning will be logged and the first definitions will be perserved.
     * 
     * @param jsonFilePaths
     * @throws IOException
     */
    protected static void populateDictionary(Collection<String> jsonFilePaths) throws IOException {
        File file;
        for (String fileName : jsonFilePaths) {
            file = new File(fileName);
            Path filePath = Paths.get(file.toURI());
            try {
                JSONArray jsonArr = new JSONArray(Files.readAllLines(filePath).stream().collect(Collectors.joining()));
                Definition.fromJSONList(jsonArr.toList()).forEach(UserDictionary::addWord);
            } catch (IOException error) {
                System.err.println("Failed to open: " + fileName);
                throw error;
            }
        }
    }

    private static void populateDictionary(String jsonFilePath) throws IOException {
        populateDictionary(List.of(jsonFilePath));
    }

    /**
     * Capitalizes the word before adding it to the inner Dictionary
     * 
     * @param str
     * @param def
     */
    public static void addWord(String word, Definition def) {
        DefinitionUtils.validateName(word);

        String name = DefinitionUtils.convertToName(word);
        Definition existingDefinition = dictionary.get(name);
        if (existingDefinition != null && existingDefinition.isPrimitive()) {
            throw new IllegalArgumentException(String.format("Error: Can not overrided primitives", word));
        }

        dictionary.put(name, def);
    }

    static void addWord(Definition def) {
        addWord(def.getName(), def);
    }

    /**
     * Throws an IllegalArgumentException if the string is blank or null.
     * 
     * @param word
     * @return Definition of the provided word
     */
    public static Definition getDefinition(String word) {
        if(initalized == false){
            throw new IllegalStateException("User Dictionary has not been initalized");
        }

        // Create custom ananymous definitions for integers
        if(Lexer.isInteger(word, 10)){
            String[] arr = ParserUtils.littleEndian(word);
            return Definition.createPrimitiveDefinition("", List.of(String.format("PUSHCELL #%s, #%s", arr[0], arr[1])));
        }

        return dictionary.get(DefinitionUtils.convertToName(word));
    }
}
