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

import org.json.JSONArray;

import com.famiforth.compiler.CompilerUtils;
import com.famiforth.exceptions.SyntaxErrorException;

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

    /**
     * Adds a newly compiled primitive word to the UserDictionary
     * @param name
     * @param words
     * @param isMacro
     */
    public static void addPrimitiveWord(String name, String label, boolean isMacro) {
        addWord(new Definition(name, label, isMacro));
    }

    /**
     * Adds a newly compiled user defined word to the UserDictionary
     * @param name
     * @param words
     * @param isMacro
     */
    public static void addUserDefinedWord(String name, boolean isMacro, final List<String> words) {
        addWord(new Definition(name, isMacro, words));
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
        final Definition def = CompilerUtils.isInteger(word, 10) ? getIntegerDefinition(word) : getDefinition(word);
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
        Definition def = dictionary.get(word);
        if(def != null){
            return def;
        }

        // If the definition is not found, check if the word is a number.
        if(CompilerUtils.isInteger(word, 10)){
            return getIntegerDefinition(word);
        }

        throw new NullPointerException(String.format("The word, %s, is not defined in the dictionary.", word));
    }

    /**
     * @param word
     * @return Definition of the provided word
    */
    public static Definition getIntegerDefinition(final String word){
        initCheck();
        final String[] arr = CompilerUtils.littleEndian(word);
        Definition def = new Definition(word, String.format("%s,%s", arr[0], arr[1]), true);
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
        return new Definition("", true, wordList);
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
            throw new NullPointerException(String.format("Error: Anonymous definitions can not be added to the dictionary"));
        }

        final Definition existingDefinition = dictionary.get(def.getName());
        if (existingDefinition != null && existingDefinition.isPrimitive()) {
            throw new IllegalArgumentException(String.format("Error: Can not override primitives"));
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
