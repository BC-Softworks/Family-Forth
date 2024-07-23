package com.famiforth;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

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

    public void addWord(String str, Definition def) {
        if(str == null){
            throw new IllegalArgumentException("Words can not be null.");
        }

        if(dictionary.get(str.toUpperCase()) != null){
            throw new IllegalArgumentException(String.format("Error: %s is already defined", str));
        }

        dictionary.put(str.toUpperCase(), def);
    }

    public Definition getDefinition(String str) {
        if(str == null){
            throw new IllegalArgumentException("No null word exists.");
        }
        return dictionary.get(str.toUpperCase());
    }
    
}
