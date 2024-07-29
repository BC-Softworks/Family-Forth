package com.famiforth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Compiler {

    final String DEFAULT_FILE_OUT = "build/out.asm";
    final String DEFAULT_DICTIONARY_LOCATION = "src/main/resources/base_dictionary.json";

    final Lexer scan;
    final Parser parser;

    String fileOut;

  public Compiler(String[] args) {
    CommandLineParser cmdLine = new DefaultParser();
    Options options = createOptions();
    Reader reader = null;
    
    try {
        CommandLine cmd = cmdLine.parse(options, args);

        if(cmd.hasOption("h")){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("familyforth [OPTION]... FILENAME", options);
            System.exit(0);
        }

        if(cmd.hasOption("o")){
            fileOut = cmd.getOptionValue("o");
        } else {
            fileOut = DEFAULT_FILE_OUT;
        }

        List<String> unparsedArgs = cmd.getArgList();
        if(unparsedArgs.isEmpty()){
            System.err.println("Error: No input file provided");
            System.exit(0);
        }

        File fileIn = new File(unparsedArgs.get(0));
        if(!fileIn.canRead()){
            System.err.println("Error: Unable to read source file.");
            System.exit(0);
        }
        reader = new FileReader(fileIn);

    } catch (FileNotFoundException | ParseException ex) {
        System.err.println("Error: " + ex.getMessage());
    }


    scan = new Lexer(reader);
    parser = new Parser(scan, DEFAULT_DICTIONARY_LOCATION, DEFAULT_FILE_OUT);
  }

    private Options createOptions() {
        Option help = new Option("h", "help", false, "print this message");
        Option out = new Option("o", "Place the output into <file>");
        return new Options().addOption(help)
                .addOption(out);
    }

    private void parseFile() throws IOException {
        parser.parse();
    }

    public static void main(String args[]) throws IOException {
        Compiler comp = new Compiler(args);
        try {
            comp.parseFile();
        } catch (Throwable e) {
            System.err.println("     " + e.getMessage());
            System.exit(1);
        }
    }

}
