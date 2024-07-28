package com.famiforth;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ParserUtils {

    // Utils class, no need to create an instance.
    ParserUtils(){}

    /**
     * Convert integer to array of 16 bit little endian hex values
     * @param input
     * @return
     */
    protected static String[] littleEndian(String input){
        String hex = integerToHex(input);
        return new String[]{hex.substring(2, 4), hex.substring(0, 2) };
    }

    /**
     * Convert integers to signed hex strings
     * Negative numbers are returned in twos complment form
     * @param input
     * @return
     */
    protected static String integerToHex(int input) {
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
            hex = Arrays.stream(hex.split("")).map(ParserUtils::invertHex).collect(Collectors.joining(""));
        }
        return hex;
    }

    protected static String integerToHex(String input) {
        return integerToHex(Integer.valueOf(input));
    }

    /**
     * Return the twos complment of a single hex character
     * @return
     */
    protected static String invertHex(String str){
        switch(str){
            case "0":
                return "F";
            case "1":
                return "E";
            case "2":
                return "D";
            case "3":
                return "C";
            case "4":
                return "B";
            case "5":
                return "A";
            case "6":
                return "9";
            case "7":
                return "8";
            case "8":
                return "7";
            case "9":
                return "6";
            case "A":
                return "5";
            case "B":
                return "4";
            case "C":
                return "3";
            case "D":
                return "2";
            case "E":
                return "1";
            case "F":
                return "0";
            default:
                throw new IllegalArgumentException("Invalid hex character.");
        }
    }
}
