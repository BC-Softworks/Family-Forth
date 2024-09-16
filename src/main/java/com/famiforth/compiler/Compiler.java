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

import com.famiforth.generator.AssemblyGenerator;
import com.famiforth.exceptions.CompilerException;
import com.famiforth.lexer.Lexer;
import com.famiforth.parser.Parser;
import com.famiforth.parser.ParserToken;
import com.famiforth.parser.ParserToken.DefinitionType;

/** FamilyForth cross compiler
 * @author Edward Conn
*/
public class Compiler {

    final private boolean guard;
    final private boolean header;
    final private boolean vector;
    final private boolean oam;
    final private byte mirror;
    final private byte mapper;
    final private byte backup;
    final private byte prgBanks;
    final private byte charBanks;
    final private File fileIn;
    final private File fileOut;
    final private File cfgFile;  // Used to validate segements
    final private String initalDictionary;
    final private List<String> parsedLibraries;

    public Compiler(boolean guard, boolean header, boolean vector, boolean oam, byte mirror, byte mapper, byte backup, byte prgBanks, byte charBanks, 
            File fileIn, File fileOut, File cfgFile, String initalDictionary) {
        this.guard = guard;
        this.header = header;
        this.vector = vector;
        this.oam = oam;
        this.mirror = mirror;
        this.mapper = mapper;
        this.backup = backup;
        this.prgBanks = prgBanks;
        this.charBanks = charBanks;
        this.fileIn = fileIn;
        this.fileOut = fileOut;
        this.cfgFile = cfgFile;
        this.initalDictionary = initalDictionary;
        this.parsedLibraries = new ArrayList<>();
    }

    public void compile() throws IOException {
        // Create a Lexer, Parser, and Generator using the parsed options
        Parser parser = null;
        AssemblyGenerator generator = null;

        try {
            Lexer lexer = new Lexer(new FileReader(fileIn));
            parser = new Parser(lexer, initalDictionary);
            fileOut.getParentFile().mkdirs();
            generator = new AssemblyGenerator(new FileOutputStream(fileOut));
        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        if(guard){
            generator.writeGuard(fileOut.getName().substring(0, fileOut.getName().lastIndexOf(".")).toUpperCase());
        }

        if(header){
            generator.writeHeader(fileOut.getName(), mapper, mirror, backup, prgBanks, charBanks);
        }

        if(vector){
            generator.writeVector(fileOut.getName());
        }
        
        if(oam){
            generator.writeOAM(fileOut.getName());
        }
        
        parseFile(parser, generator);

        // Close FileOutputStream
        generator.close();
        System.out.println("Program compiled successful.");
    }

    private void parseFile(Parser parser, AssemblyGenerator generator) throws IOException {
        List<ParserToken> parsedWords = new ArrayList<>();
        while(parser.hasNext()){
            ParserToken token = parser.parse();
            if(token != null){
                // If a requires or include is encountered pause parsing the 
                // current file and parse that file first.
                if( ParserToken.DefinitionType.REQUIRE.equals(token.type) || 
                    ParserToken.DefinitionType.INCLUDE.equals(token.type)){
                    parseParentFile(token.reference.getLeft(), token.type);
                }
                parsedWords.add(token);
            }
        }

        for(ParserToken token : parsedWords){
            generator.generate(token);
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
            AssemblyGenerator generator = new AssemblyGenerator(new FileOutputStream(fOut));
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
     * TODO: Support other file extensions
     */
    private Reader findRequiredFile(String fileInName) throws IOException {
        InputStream stream = null;
        FileFilter filter = file -> file.getName().startsWith(fileInName) && file.getName().endsWith(".f");

        // Check the current directory first
        File requiredFile = new File(fileIn.getParent());
        File[] matches = requiredFile.listFiles(filter);
        if(matches.length > 0){
            stream = new FileInputStream(matches[0]);
        } else if (fileInName.startsWith("lib")){
            // Then check the lib
            stream = ClassLoader.getSystemResourceAsStream("lib" + File.separator + fileInName);
        } else {
            // Then check the kernel
            stream = ClassLoader.getSystemResourceAsStream("kernel" + File.separator + fileInName);
        }
        
        return new BufferedReader(new InputStreamReader(stream));
    }
}
