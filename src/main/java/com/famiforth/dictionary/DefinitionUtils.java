package com.famiforth.dictionary;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.exceptions.SyntaxErrorException;

public class DefinitionUtils {
    
    /**
     * Capitalizes the word and then replaces the special charaters in the word.
     * @param word
     * @return a valid {@link Definition} name
     */
    public static String convertToName(String word){
        validateName(word);
        return replaceSpecialCharacters(word.toUpperCase());
    }

    /**
     * Throws an exception if the provided word is invalid.
     * @param word
     */
    public static void validateName(String word){
        if (word == null) {
            throw new IllegalArgumentException("Words can not be null.");
        }

        // Throw an error is a fobidden character is found.
        if (StringUtils.containsAny(word, "^`{|}~[]\\\"")) {
            throw new SyntaxErrorException("Illegal character in name: " + word);
        }
    }

    /**
     * Replaces the special charaters in the word
     * @param word the word to be validated
     * @return the word with special characters replaced with lowercase letters
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
