package com.famiforth.dictionary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;

import com.famiforth.compiler.LexerUtils;

/**
 * A singleton object containing all words currently known by the compiler
 * A special condition exists for integers which have anonymous definitions 
 */
public class UserDictionary {
    private static boolean initalized = false;
    private static UserDictionary instance = null;
    private static Dictionary<String, Definition> dictionary;

    /**
     * Create instance of singleton dictionary
     * 
     * @param fileName
     * @return UserDictionary instance
     */
    public static UserDictionary initalize(final String fileName) {
        if (instance == null) {
            try {
                instance = new UserDictionary(fileName);
            } catch (final IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
            initalized = true;
        }
        return instance;
    }

    /**
     * Adds a newly compiled primitive word to the UserDictionary
     * @param name
     * @param words
     */
    public static void addPrimitiveWord(String name, String label, boolean isMacro) {
        addWord(new Definition(name, label, isMacro));
    }

    /**
     * Adds a newly compiled user defined word to the UserDictionary
     * @param name
     * @param words
     */
    public static void addUserDefinedWord(String name, String label, boolean isMacro, final List<String> words) {
        addWord(new Definition(name, label, isMacro, words));
    }

    public static void addUserDefinedWord(String name, boolean isMacro, final List<String> words) {
        addWord(new Definition(name, isMacro, words));
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     * 
     * @param word
     * @return name of the subroutine stored in the Dictionary
     */
    public static String getSubroutine(final String name) {
        assert !LexerUtils.isInteger(name, 10);
        assert !isMacro(name);

        initCheck();
        return "jsr " + getDefinition(name).getLabel();
    }


    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     * 
     * Fetches the Definition of the word from the dictionary 
     * and checks if it is a macro
     * @param word
     */
    public static boolean isMacro(final String word) {
        initCheck();
        return getDefinition(word).isMacro();
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     * 
     * @param word
     * @return Fully flattened definition as a list of assembly opcodes and operands
     */
    public static List<String> getFlattenedDefinition(final String word) {
        initCheck();
        final Definition def = LexerUtils.isInteger(word, 10) ? getIntegerDefinition(word) : getDefinition(word);
        return def.flattenDefinition();
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     * @param word
     * @return Definition of the provided word
     */
    public static Definition getDefinition(final String word) {
        initCheck();
        return dictionary.get(word);
    }

    /**
     * @param word
     * @return Definition of the provided word
    */
    static Definition getIntegerDefinition(final String word){
        initCheck();
        final String[] arr = LexerUtils.littleEndian(word);
        return new Definition(word, String.format("PUSHCELL #%s, #%s", arr[0], arr[1]), true);
    }

    /**
     * Populate the current dictionary instance with the
     * contents of the provided JSON files. If a word is encountered twice,
     * a warning will be logged and the first definitions will be perserved.
     * 
     * @param jsonFilePaths
     * @throws IOException
     */
    private static void populateDictionary(final Collection<String> jsonFilePaths) throws IOException {
        File file;
        for (final String fileName : jsonFilePaths) {
            file = new File(fileName);
            final Path filePath = Paths.get(file.toURI());
            try {
                final JSONArray jsonArr = new JSONArray(Files.readAllLines(filePath).stream().collect(Collectors.joining()));
                Definition.fromJSONList(jsonArr.toList()).forEach(UserDictionary::addWord);
            } catch (final IOException error) {
                System.err.println("Failed to open: " + fileName);
                throw error;
            }
        }
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     */
    private static void initCheck() {
        if(initalized == false){
            throw new IllegalStateException("User Dictionary has not been initalized");
        }
    }
    
    private static void populateDictionary(final String jsonFilePath) throws IOException {
        populateDictionary(List.of(jsonFilePath));
    }

    private static void addWord(final Definition def) {
        final Definition existingDefinition = dictionary.get(def.getName());
        if (existingDefinition != null && existingDefinition.isPrimitive()) {
            throw new IllegalArgumentException(String.format("Error: Can not override primitives"));
        }

        dictionary.put(def.getName(), def);
    }

    // Prevent the creation of an empty dictionary
    private UserDictionary() {
    }

    private UserDictionary(final String fileName) throws IOException {
        dictionary = new Hashtable<String, Definition>();
        populateDictionary(fileName);
    }
}
