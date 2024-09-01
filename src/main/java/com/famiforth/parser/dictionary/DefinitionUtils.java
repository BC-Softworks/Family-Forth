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
        final String specialCharString = "$%&~:;?";
        final String lowercaseAlphabet = "klmnopq";
        return Arrays.asList(word.split("")).stream().map(str -> {
            // Digits
            if("0".equals(str)){
                return "zero";
            }
            if("1".equals(str)){
                return "one";
            }
            if("2".equals(str)){
                return "two";
            }
            if("3".equals(str)){
                return "three";
            }
            if("4".equals(str)){
                return "four";
            }
            if("5".equals(str)){
                return "five";
            }
            if("6".equals(str)){
                return "six";
            }
            if("7".equals(str)){
                return "seven";
            }
            if("8".equals(str)){
                return "eight";
            }
            if("9".equals(str)){
                return "nine";
            }

            // Special symbols
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
            if("'".equals(str)){
                return "tick";
            }

            if(specialCharString.contains(str)) {
                int index = specialCharString.indexOf(str);
                return lowercaseAlphabet.substring(index, index + 1);
            }

            return str;
        }).collect(Collectors.joining());
    }
}
