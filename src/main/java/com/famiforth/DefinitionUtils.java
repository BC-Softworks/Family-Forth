package com.famiforth;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class DefinitionUtils {
    
    public static void validateName(String word){
        if (word == null) {
            throw new IllegalArgumentException("Words can not be null.");
        }

        // Throw an error is a fobidden character is found.
        if (StringUtils.containsAny(word, "^`{|}~[]\\\"")) {
            throw new IllegalArgumentException("Illegal character in name: " + word);
        }
    }

    public static String convertToName(String word){
        // Set word to uppercase
        // Map special characters to lowercase values
        return replaceSpecialCharacters(word.toUpperCase());
    }

    /**
     * Replace the special charaters in the word
     * Words map directly to subroutines and special characters are forbidden in cc65 proc labels
     * @param word
     * @return 
     */
    private static String replaceSpecialCharacters(String word) {
        return Arrays.asList(word.split("")).stream().map(str -> {
            String specialCharString = "!#$%&'()*+,<:;/.?=>@_-~";
            String lowercaseAlphabet = "abcdefghijklmnopqrstuvw";
            if(specialCharString.contains(str)) {
                int index = specialCharString.indexOf(str);
                return lowercaseAlphabet.substring(index, index + 1);
            }
            return str;
        }).collect(Collectors.joining());
    }
}
