package com.famiforth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.famiforth.Lexer.Keyword;
import com.famiforth.Lexer.Token;
import com.famiforth.Lexer.TokenType;
import com.famiforth.exceptions.SyntaxErrorException;

/** FamilyForth Parser
 * @author Edward Conn
*/
public class Parser {

    final private Lexer lexer;
    final private List<List<String>> assemblyListOut;

    final String fileName;
    FileOutputStream fileOut;
    final static List<String> defaultFilesToInclude = Arrays.asList("core.asm");


    public Parser(Lexer scan, String dictionaryFile, String fileName) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
        this.fileName = fileName;
        assemblyListOut = new LinkedList<>();
    }

    public List<List<String>> getAssemblyListOut() {
        return assemblyListOut;
    }

    public void parse() throws IOException {
        fileOut = new FileOutputStream(new File(fileName));
        writeFileHeader(fileOut, defaultFilesToInclude);
        while (lexer.hasNext()) {
            final List<String> assemblyList = new LinkedList<>();

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
                        parseColonStatement();
                    } else {
                        assemblyList.addAll(parseToken(token));
                    }
                    break;
                case FLOAT:
                    throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
                case INTEGER:
                case WORD:
                    assemblyList.addAll(parseToken(token));
                    break;
                default:
                    break;
            }

            // Used for debugging
            // Disable after tests are implemented
            assemblyListOut.add(List.copyOf(assemblyList));
            fileOut.write((StringUtils.join(assemblyList, System.lineSeparator()) + System.lineSeparator()).getBytes());
            assemblyList.clear();
        }
        fileOut.close();
    }

    private static void writeFileHeader(FileOutputStream fileOut, List<String> filesToInclude) throws IOException {
        String includeBlock = filesToInclude.stream().map(str -> String.format(".include \"%s\"", str)).collect(Collectors.joining(System.lineSeparator()));
        fileOut.write((includeBlock + System.lineSeparator()).getBytes());
    }

    /**
     * Get the Token's Definition from the dictionary
     * Throws an error if no Definition is found
     * @param token
     * @return 
     */
    private List<String> parseToken(Token token) {
        return UserDictionary.getDefinition(token.value).flattenDefinition();
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
    private void parseColonStatement() throws IOException {
        String name = lexer.next_token().value;
        if (Keyword.getByValue(name) == Keyword.SEMICOLON) {
            throw new SyntaxErrorException("Empty definition");
        }

        List<String> wordList = new LinkedList<>();
        Token token = lexer.next_token();
        while (Keyword.getByValue(token.value) != Keyword.SEMICOLON) {
            wordList.add(token.value);
            token = lexer.next_token();
        }

        // Add the word to the dictionary
        UserDictionary.addWord(Definition.createUserWordDefinition(name, wordList));
    }
}
