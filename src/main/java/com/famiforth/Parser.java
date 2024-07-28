package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.Lexer.Keyword;
import com.famiforth.Lexer.Token;
import com.famiforth.Lexer.TokenType;

public class Parser {

    final private Lexer lexer;
    final private List<List<String>> parsedDefinitions;

    final File fileOut;
    final static String DEFAULT_FILE_OUT = "out.asm";


    public Parser(Lexer scan, String dictionaryFile, String fileName) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
        fileOut = new File(fileName);
        parsedDefinitions = new LinkedList<>();
    }

    public Parser(Lexer scan, String dictionaryFile) {
        this(scan, dictionaryFile, DEFAULT_FILE_OUT);
    }

    public List<List<String>> getParsedDefinitions() {
        return parsedDefinitions;
    }

    public void parse() throws IOException {
        while (lexer.hasNext()) {
            List<String> assemblyList = new ArrayList<>();

            Token token = lexer.next_token();
            switch (token.type) {
                case SKIP_LINE:
                    lexer.skipLine();
                    continue;
                case BEGIN_COMMENT:
                    while (token.type != TokenType.END_COMMENT) {
                        token = lexer.next_token();
                    }
                    continue;
                case KEYWORD:
                    Keyword keyword = Keyword.getByValue(token.value);
                    if(keyword.equals(Keyword.COLON)){
                        assemblyList = parseColonStatement();
                    } else {
                        assemblyList = parseWord(token);
                    }
                    break;
                case FLOAT:
                    throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
                case INTEGER:
                    assemblyList = List.of("PUSH " + integerToHex(token.value));
                    break;
                case WORD:
                    assemblyList = parseWord(token);
                    break;
                default:
                    break;
            }

            // Used for debugging
            // Disable after tests are implemented
            parsedDefinitions.add(assemblyList);

            // TODO: Write to file instead of System.out
            System.out.println(StringUtils.join(assemblyList, System.lineSeparator()));
        }
    }

    /**
     * Converts the provided Definition to a List of Strings
     * @param definition
     * @return
     */
    public List<String> expandDefinition(Definition definition) {
        if(definition == null){
            throw new SyntaxErrorException("Unable to access an undefined word.");
        }

        if (definition.isPrimitive()) {
            return definition.getAssembly();
        }

        List<String> list = new ArrayList<>();
        for (Definition def : definition.getWords()) {
            list.addAll(expandDefinition(def));
        }
        return list;
    }

    /**
     * Get the Token's Definition from the dictionary
     * Throws an error if no Definition is found
     * @param token
     * @return 
     */
    private List<String> parseWord(Token token) {
        Definition word = UserDictionary.getDefinition(token.value);
        return expandDefinition(word);
    }

    /**
     * ( C: "<spaces>name" -- colon-sys )
     * Skip leading space delimiters. Parse name delimited by a space. 
     * Create a definition for name, called a "colon definition".
     * Enter compilation state and start the current definition, producing colon-sys.
     * Append the initiation semantics given below to the current definition.
     * The execution semantics of name will be determined by the words compiled into the body of the definition. 
     * The current definition shall not be findable in the dictionary until it is ended (or until the execution of DOES> in some systems). 
     * 
     * @return
     * @throws IOException
     */
    private List<String> parseColonStatement() throws IOException {
        String name = lexer.next_token().value;
        if (Keyword.getByValue(name) == Keyword.SEMICOLON) {
            throw new SyntaxErrorException("Empty definition");
        }

        List<String> procedureList = new ArrayList<>();
        Token token = lexer.next_token();
        while (Keyword.getByValue(token.value) != Keyword.SEMICOLON) {
            procedureList.add(token.value);
            token = lexer.next_token();
        }

        // Add the word to the dictionary
        UserDictionary.addWord(name, Definition.createUserWordDefinition(name, procedureList.stream()
                .map(UserDictionary::getDefinition)
                .collect(Collectors.toList())));

        return procedureList;
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
            hex = Arrays.stream(hex.split("")).map(Parser::invertHex).collect(Collectors.joining(""));
        }
        return hex;
    }

    private static String integerToHex(String input) {
        return integerToHex(Integer.valueOf(input));
    }

    /**
     * Return the twos complment of a single hex character
     * @return
     */
    private static String invertHex(String str){
        System.out.print("Here");
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
