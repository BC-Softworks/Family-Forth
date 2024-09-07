package com.famiforth.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.famiforth.generator.AssemblyGenerator;
import com.famiforth.exceptions.CompilerException;
import com.famiforth.generator.AbstractGenerator;
import com.famiforth.lexer.Lexer;
import com.famiforth.parser.Parser;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.ParserToken.DefinitionType;

/** FamilyForth cross compiler
 * @author Edward Conn
*/
public class Compiler {

    private File fileIn;
    private File fileOut;
    private String initalDictionary;
    private List<String> parsedLibraries;

    public Compiler(File fileIn, File fileOut, String initalDictionary) {
        this.fileIn = fileIn;
        this.fileOut = fileOut;
        this.initalDictionary = initalDictionary;
        parsedLibraries = new ArrayList<>();
    }

    public void compile() throws IOException {
        // Create a Lexer, Parser, and Generator using the parsed options
        Parser parser = null;
        AbstractGenerator generator = null;

        try {
            Lexer lexer = new Lexer(new FileReader(fileIn));
            parser = new Parser(lexer, initalDictionary);
            fileOut.getParentFile().mkdirs();
            generator = new AssemblyGenerator(new FileOutputStream(fileOut));
        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        generator.writeGuard(fileOut.getName().substring(0, fileOut.getName().lastIndexOf(".")).toUpperCase());
        parseFile(parser, generator);

        // Close FileOutputStream
        generator.close();
        System.out.println("Program compiled successful.");
    }

    private void parseFile(Parser parser, AbstractGenerator generator) throws IOException {
        // Used for debugging
        List<ParserToken> parsedWords = new Stack<>();
        while(parser.hasNext()){
            ParserToken token = parser.parse();
            if(token != null){
                // If a requires or include is encountered pause parsing the 
                // current file and parse that file first.
                if(ParserToken.DefinitionType.REQUIRE.equals(token.type) || ParserToken.DefinitionType.INCLUDE.equals(token.type)){
                    parseParentFile(token.reference.getLeft(), token.type);
                }

                generator.generate(token);
                // Used for debugging
                parsedWords.add(token);
            }
        }
    }

    /**
     * Parse the parent file
     * If required, generate a seperate assembly outfile,
     * if and only if one has not already been generated.
     * @param fileIn
     * @param type
     * @throws IOException
     */
    private void parseParentFile(String fileInName, DefinitionType type) throws IOException {
        Reader fileIn = null;
        try{
            fileIn = findRequiredFile(fileInName);
        } catch(NullPointerException ex) {
            throw new CompilerException("Unable to find the required file: " + fileInName, ex);
        } catch(IOException ex) {
            throw new CompilerException("Unable to open the required file: " + fileInName, ex);
        }

        final Lexer lexer = new Lexer(fileIn);
        final Parser parser = new Parser(lexer);

        String libraryName = fileInName.substring(0, fileInName.indexOf("."));
        if(!parsedLibraries.contains(libraryName)){
            parsedLibraries.add(libraryName);
            String fileOutName = libraryName + ".asm";
            File fOut = new File(fileOut.getParent() + File.separator + fileOutName);
            fOut.getParentFile().mkdirs();
            AbstractGenerator generator = new AssemblyGenerator(new FileOutputStream(fOut));
            generator.writeGuard(libraryName.toUpperCase());
            parseFile(parser, generator);
            generator.close();
        }
    }

    /**
     * @param fileInName
     * @return the require file with the given name
     * @throws IOException if the required file can not be opened
     * @throws NullPointerException if the required file is not found 
     */
    private Reader findRequiredFile(String fileInName) throws IOException {
        InputStream stream = null;
        FileFilter filter = file -> file.getName().startsWith("fileInName") && file.getName().endsWith(".f");
        // Check the current directory first
        File[] matches = new File(fileIn.getParent()).listFiles(filter);
        if(matches.length > 0){
            stream = new FileInputStream(matches[0]);
        } else {
            // Then check the kernel
            stream = ClassLoader.getSystemResourceAsStream("kernel" + File.separator + fileInName);
        }
        
        return new BufferedReader(new InputStreamReader(stream));
    }
}
