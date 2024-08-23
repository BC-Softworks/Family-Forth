package com.famiforth.parser.dictionary;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.exceptions.SyntaxErrorException;

public class DefinitionUtils {
    
    /**
     * Capitalizes the word and then replaces the special charaters in the word.
     * Also, in order to save space the label is trimmed to 16 characters max
     * This can cause conflicts and should be noted in the documentation
     * @param word
     * @return a valid {@link Definition} name
     */
    public static String convertToVaildLabel(String word){
        validateName(word);
        word = word.substring(0, Math.min(word.length(), 16));
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
        if (StringUtils.containsAny(word, "^`(){|}~[]\\\"")) {
            throw new SyntaxErrorException("Illegal character in name: " + word);
        }
    }

    /**
     * Replaces the special charaters in the word
     * @param word the word to be validated
     * @return the word with special characters replaced with lowercase letters
     */
    private static String replaceSpecialCharacters(String word) {
        final String specialCharString = "1234567890$%&~:;?";
        final String lowercaseAlphabet = "abcdefghijklmnopq";
        return Arrays.asList(word.split("")).stream().map(str -> {
            if("!".equals(str)){
                return "store";
            }
            if("@".equals(str)){
                return "fetch";
            }
            if("<".equals(str)){
                return "less";
            }
            if("=".equals(str)){
                return "equal";
            }
            if(">".equals(str)){
                return "great";
            }
            if("+".equals(str)){
                return "plus";
            }
            if("-".equals(str)){
                return "minus";
            }
            if("*".equals(str)){
                return "star";
            }
            if("/".equals(str)){
                return "slash";
            }
            if(",".equals(str)){
                return "comma";
            }

            if(specialCharString.contains(str)) {
                int index = specialCharString.indexOf(str);
                return lowercaseAlphabet.substring(index, index + 1);
            }

            return str;
        }).collect(Collectors.joining());
    }
}
