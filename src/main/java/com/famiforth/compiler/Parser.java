package com.famiforth.compiler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.famiforth.compiler.Lexer.Keyword;
import com.famiforth.compiler.Lexer.Token;
import com.famiforth.compiler.Lexer.TokenType;
import com.famiforth.dictionary.Definition;
import com.famiforth.dictionary.UserDictionary;
import com.famiforth.exceptions.SyntaxErrorException;

/** FamilyForth Parser
 * @author Edward Conn
*/
public class Parser {

    final private Lexer lexer;

    public Parser(Lexer scan, String dictionaryFile) {
        UserDictionary.initalize(dictionaryFile);
        this.lexer = scan;
    }

    public boolean hasNext() {
        return lexer != null && lexer.hasNext();
    } 

    public List<String> parse() throws IOException {
        List<String> result = null;

        while(result == null && lexer.hasNext()){
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
                        result = parseToken(token);
                    }
                    break;
                case FLOAT:
                    throw new SyntaxErrorException("Floating point numbers are currently unsupported.");
                case INTEGER:
                case WORD:
                    result = parseToken(token);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    /**
     * Get the Token's Definition from the dictionary
     * Throws an error if no Definition is found
     * @param token
     * @return 
     */
    private List<String> parseToken(Token token) {
        // TOD: Create custom anonymous definitions for integers
        if(TokenType.INTEGER.equals(token.type)){
            String[] arr = ParserUtils.littleEndian(token.value);
            return List.of(String.format("PUSHCELL #%s, #%s", arr[0], arr[1]));
        } 
        
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
