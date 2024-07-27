package com.famiforth;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;

public class UserDictionary {
    private static UserDictionary instance = null;
    private static Dictionary<String, Definition> dictionary;

    // Prevent the creation of an empty dictionary
    private UserDictionary() {}

    private UserDictionary(String fileName) throws IOException {
        
        FileInputStream fin = null;
        
        try {
            dictionary = new Hashtable<String, Definition>();
            fin = new FileInputStream(fileName);
            
        } catch(IOException e){
            System.err.println(String.format("Unable to open the dictionary file: %s", fileName));
        } finally {
            if(fin != null){
                fin.close();
            }
        }
    }

    public static UserDictionary getInstance(String fileName){
        if(instance == null){
            try {
                instance = new UserDictionary(fileName);
            } catch(IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    /**
     * Populate the current dictionary instance with the
     * contence of the provided JSON files. If a word is encountered twice,
     * a warning will be logged and the first definitions will be perserved.
     * @param jsonFilePaths
     * @throws IOException 
     */
    protected static void populateDictionary(Collection<String> jsonFilePaths) throws IOException {
        FileReader reader = null;
        for(String fileName : jsonFilePaths){
            try {
                reader = new FileReader(fileName);

                
            } catch(IOException error) {
                System.err.println("Failed to open: " + fileName);
                throw error;
            } finally {
                if(reader != null){
                    reader.close();
                }
            }
        }
    }

    /**
     * Capitalizes the word before adding it to the inner Dictionary
     * @param str
     * @param def
     */
    public void addWord(String word, Definition def) {
        if(word == null){
            throw new IllegalArgumentException("Words can not be null.");
        }

        if(dictionary.get(word.toUpperCase()) != null){
            throw new IllegalArgumentException(String.format("Error: %s is already defined", word));
        }

        dictionary.put(word.toUpperCase(), def);
    }

    /**
     * Throws an IllegalArgumentException if the string is blank or null.
     * @param word
     * @return
     */
    public Definition getDefinition(String word) {
        if(StringUtils.isBlank(word)){
            throw new IllegalArgumentException("No null word exists.");
        }
        return dictionary.get(word.toUpperCase());
    }
    
}
