package com.famiforth.parser.dictionary;

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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import com.famiforth.compiler.CompilerUtils;
import com.famiforth.exceptions.SyntaxErrorException;

/**
 * A singleton object containing all words currently known by the compiler
 * A special condition exists for integers which have anonymous definitions 
 */
public class UserDictionary {
    private static final String PUSH = "PUSH";
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
                instance = fileName == null ? new UserDictionary() : new UserDictionary(fileName);
            } catch (final IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
            initalized = true;
        }
        return instance;
    }

    public static UserDictionary initalize() {
        return initalize(null);
    }

    public static void empty() {
        instance = null;
    }

    /**
     * Adds a newly compiled user defined word to the UserDictionary
     * @param name
     * @param words
     * @param isMacro
     */
    public static void addWord(String name, boolean isMacro, boolean isPrimitive, final List<String> words) {
        Definition def = new Definition(name, isMacro, isPrimitive, words);
        if(StringUtils.containsWhitespace(name)){
            if(isMacro){
                def.setHasArgs();
            } else {
                throw new SyntaxErrorException("User defined words can not contain whitespace.");
            }
        }
        addWord(def);
    }

    public static void addWord(String name, boolean isMacro, final List<String> words) {
        addWord(name, isMacro, false, words);
    }


    /**
     * Adds an empty user defined word to the UserDictionary
     * @param name
     * @param words
     * @param isMacro
     */
    public static void addEmptyWord(String name) {
        addWord(new Definition(name, false, false, List.of()));
    }

    /**
     * Sets a word's isImmediate flag to true
     * @param name
     */
    public static void modifyUserDefinedWord(String name) {
        Definition def = getDefinition(name);
        def.setIsImmediate();
        addWord(def);
    }

    /**
     * Fetches the Definition of the word from the dictionary 
     * and checks if it is a macro
     * @param word
     * @throws IllegalStateException if the UserDictionary has not be initialized
     * @throws NullPointerException if the word is not defined in the UserDictionary
     */
    public static boolean isMacro(final String word) {
        initCheck();
        return getDefinition(word).isMacro();
    }

    /**
     * 
     * @param word
     * @return Fully flattened definition as a list of assembly opcodes and operands
     * @throws IllegalStateException if the UserDictionary has not be initialized
     * @throws SyntaxErrorException if the word is not defined in the UserDictionary
     */
    public static List<String> getFlattenedDefinition(final String word) {
        initCheck();
        Definition def = null;
        if(CompilerUtils.isDecimal(word)) {
            def = getIntegerDefinition(word, true);
        } else if(CompilerUtils.isHex(word)) {
            def = getIntegerDefinition(word, false);
        } else {
            def = getDefinition(word);
        }

        if(def == null){
            throw new SyntaxErrorException("Undefined word \"" + word + "\" referenced.");
        }
        return def.flattenDefinition();
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     * @param word
     * @return Definition of the provided word
     * @throws NullPointerException if the word is not defined in the UserDictionary
     */
    public static Definition getDefinition(final String word) {
        initCheck();
        Definition def = dictionary.get(word.toUpperCase());
        if(def != null){
            return def;
        }

        // If the definition is not found, check if the word is a number.
        if(CompilerUtils.isDecimal(word)){
            return getIntegerDefinition(word, true);
        } else if(CompilerUtils.isHex(word)){
            return getIntegerDefinition(word, false);
        }

        throw new NullPointerException(String.format("The word, %s, is not defined in the dictionary.", word));
    }

    /**
     * @param word
     * @return Definition of the provided word
    */
    public static Definition getIntegerDefinition(final String word, boolean isDecimal){
        initCheck();
        Definition def = null;
        if(isDecimal){
            String[] arr = CompilerUtils.littleEndian(word);
            def = new Definition(word, String.format(PUSH + " #$%s, #$%s", arr[0], arr[1]), true);
        } else {
            String padded = CompilerUtils.padHex(word.substring(1));
            def = new Definition(word, String.format(PUSH + " #$%s, #$%s", padded.substring(2, 4), padded.substring(0, 2)), true);
        }
        def.setIsNumber();
        return def;
    }

    public static boolean isDefined(final String word){
        initCheck();
        Definition def = dictionary.get(word);
        return def != null;
    }

    /**
     * @param label
     * @return A one use definition
    */
    public static Definition getAnonymousDefinition(final String label){
        return new Definition("", label, true);
    }

    public static Definition getAnonymousDefinition(final List<String> wordList){
        return new Definition("", true, true, wordList);
    }

    /**
     * Throws an IllegalStateException if the Dictionary has not be initialized
     */
    private static void initCheck() {
        if(initalized == false){
            throw new IllegalStateException("User Dictionary has not been initalized");
        }
    }
    
    /**
     * Adds a word to the Dictionary.
     * @param def
     * 
     * @throws NullPointerException if attempting to add an anonymous definition
     * @throws IllegalArgumentException if attempting to override a primitive definition
     */
    private static void addWord(final Definition def) {
        if(def.getName() == null || def.getName().isEmpty()){
            throw new NullPointerException("Error: Anonymous definitions can not be added to the dictionary");
        }

        final Definition existingDefinition = dictionary.get(def.getName());
        if (existingDefinition != null && existingDefinition.isPrimitive()) {
            throw new IllegalArgumentException("Error: Can not override primitives");
        }

        dictionary.put(def.getName(), def);
    }

    // Prevent the creation of an empty dictionary
    private UserDictionary() {
        dictionary = new Hashtable<String, Definition>();
    }

    private UserDictionary(final String fileName) throws IOException {
        dictionary = new Hashtable<String, Definition>();
        populateDictionary(fileName);
    }

    private static void populateDictionary(final String jsonFilePath) throws IOException {
        populateDictionary(List.of(jsonFilePath));
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
}
