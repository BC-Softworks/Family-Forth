package com.famiforth.compiler;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class CompilerUtils {

    /**
     * Check if a string is an integer
     * Max and min values are not checked
     * @param s
     * @param radix
     */
    public static boolean isInteger(String s, int radix) {
        // Reject empty Strings
        if(StringUtils.isBlank(s)){
            return false;
        }

        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && (s.charAt(i) == '-' || s.charAt(i) == '+')) {
                // Ignore leading sign unless it is the only symbol
                if(s.length() == 1) {
                    return false;
                }
            } else if(Character.digit(s.charAt(i), radix) < 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string is an floating point number
     * These are not yet supported and are rejected by the parser
     * @param s
     * @param radix
     */
    public static boolean isFloat(String s) {
        // Reject empty Strings
        if(StringUtils.isBlank(s)){
            return false;
        }

        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && (s.charAt(i) == '-' || s.charAt(i) == '+')) {
                // Ignore leading sign unless it is the only symbol
                if(s.length() == 1) {
                    return false;
                }
            } else if(!(Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')){
                return false;
            }
        }
        return true;
    }

    /**
     * Convert Integer String to array of 16 bit little endian hex values
     * @param input Integer 
     * @return Integer as String array of hex bytes
     */
    public static String[] littleEndian(String input){
        String hex = integerToHex(input);
        return new String[]{hex.substring(2, 4), hex.substring(0, 2) };
    }

    /**
     * Convert Integer String to array of 16 bit little endian hex values
     * @param input Integer 
     * @return Integer as String array of hex bytes
     */
    public static String[] littleEndian(Integer input){
        String hex = integerToHex(input);
        return new String[]{hex.substring(2, 4), hex.substring(0, 2) };
    }

    /**
     * Convert integers to signed hex strings
     * Negative numbers are returned in twos complment form
     * @param input
     * @return hex string
     */
    public static String integerToHex(int input) {
        boolean nonNegative = input >= 0;
        // If negative add one
        if(!nonNegative){
            input++;
        }

        String hex = Integer.toString(Math.abs(input), 16).toUpperCase();
        // Pad the hex value with zeros
        while(hex.length() % 4 != 0){
            hex = "0" + hex;
        }

        // Twos complment
        if(!nonNegative){
            hex = Arrays.stream(hex.split(""))
                        .map(CompilerUtils::invertHex)
                        .collect(Collectors.joining(""));
        }
        return hex;
    }

    protected static String integerToHex(String input) {
        return integerToHex(Integer.valueOf(input));
    }

    /**
     * Return the twos complment of a single hex character
     * @return The inversion of the given hex character
     * @throws NullPointerException if str is null
     */
    protected static String invertHex(String str){
        final String hexNumerals = "0123456789ABCDEF";
        int index = 15 - hexNumerals.indexOf(str);
        return hexNumerals.substring(index, index + 1) ;
    }

    // Utils class, no need to create an instance.
    CompilerUtils(){}
}
