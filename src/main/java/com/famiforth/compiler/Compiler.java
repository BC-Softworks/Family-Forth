package com.famiforth.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import com.famiforth.generator.AssemblyGenerator;
import com.famiforth.generator.AbstractGenerator;
import com.famiforth.lexer.Lexer;
import com.famiforth.parser.Parser;
import com.famiforth.parser.ParserToken;

/** FamilyForth cross compiler
 * @author Edward Conn
*/
public class Compiler {

    private Parser parser;
    private AbstractGenerator generator;

    public Compiler(String fileIn, String fileOut, String initalDictionary) {

        // Create a Lexer, Parser, and Generator using the parsed options
        try {
            Lexer lexer = new Lexer(new FileReader(new File(fileIn)));
            parser = new Parser(lexer, initalDictionary);
            File fOut = new File(fileOut);
            fOut.getParentFile().mkdirs();
            generator = new AssemblyGenerator(new FileOutputStream(fOut));
        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    public void compile() throws IOException {
        // Used for debugging
        List<ParserToken> parsedWords = new Stack<>();

        while(parser.hasNext()){
            ParserToken token = parser.parse();
            if(token != null){
                generator.generate(token);
                // Used for debugging
                parsedWords.add(token);
            }
        }

        // Close FileOutputStream
        generator.close();
        System.out.println("Program compiled successful.");
    }
}
