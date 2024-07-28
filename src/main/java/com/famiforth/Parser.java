package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.Lexer.Keyword;
import com.famiforth.Lexer.Token;
import com.famiforth.Lexer.TokenType;
import com.famiforth.exceptions.SyntaxErrorException;

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
            List<String> assemblyList = new LinkedList<>();

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
                    String[] arr = ParserUtils.littleEndian(token.value);
                    assemblyList = List.of(String.format("PUSH #%s, #%s", arr[0], arr[1]));
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

        List<String> list = new LinkedList<>();
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

        List<String> assemblyList = new LinkedList<>();
        Token token = lexer.next_token();
        while (Keyword.getByValue(token.value) != Keyword.SEMICOLON) {
            assemblyList.add(token.value);
            token = lexer.next_token();
        }

        // Add the word to the dictionary
        UserDictionary.addWord(name, Definition.createUserWordDefinition(name, assemblyList.stream()
                .map(UserDictionary::getDefinition)
                .collect(Collectors.toList())));

        return assemblyList;
    }
}
