package com.famiforth;

import java.util.Dictionary;
import java.util.Hashtable;

public class UserDictionary {
    private Dictionary<String, Definition> dictionary;

    public UserDictionary() {
        dictionary = new Hashtable<String,Definition>();
    }

    public void addWord(String str, Definition def) {
        if(str == null){
            throw new IllegalArgumentException("Words can not be null.");
        }

        if(this.dictionary.get(str.toUpperCase()) != null){
            throw new IllegalArgumentException(String.format("Error: %0 is already defined", str));
        }

        this.dictionary.put(str.toUpperCase(), def);
    }

    public void getWord(String str, Definition def) {
        if(str == null){
            throw new IllegalArgumentException("No null word exists.");
        }
        this.dictionary.get(str.toUpperCase());
    }
    
}
