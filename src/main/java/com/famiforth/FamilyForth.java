package com.famiforth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.famiforth.compiler.Compiler;
import com.famiforth.compiler.CompilerBuilder;


public class FamilyForth {

    private static CompilerBuilder parseArguments(String args[]){
        CompilerBuilder builder = new CompilerBuilder();
        CommandLineParser cmdLine = new DefaultParser();
        Options options = createOptions();

        String fileOutName = null;

        try {
            CommandLine cmd = cmdLine.parse(options, args);

            if(cmd.hasOption("help")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("familyforth [OPTION]... FILENAME", options);
                System.exit(0);
            }

            if(cmd.hasOption("input")){
                File fileIn = new File(cmd.getOptionValue("input"));
                if(!fileIn.canRead()){
                    System.err.println("Error: Unable to read source file.");
                    System.exit(0);
                }
                fileOutName = fileIn.getName().substring(0, fileIn.getName().lastIndexOf(".")) + ".asm";
                builder.setFileIn(fileIn);
            } else {
                System.err.println("Error: No input file provided");
                System.exit(0);
            }

            // NES Config File
            if(cmd.hasOption("config")){
                File cfgFile = new File(cmd.getOptionValue("config"));
                if(!cfgFile.canRead()){
                    System.err.println("Error: Unable to read cfg file.");
                    System.exit(0);
                }
                builder.setCfgFile(cfgFile);
            }

            if(cmd.hasOption("header")){
                builder.setHeader(true);
            }

            if(cmd.hasOption("mapper")){
                builder.setMapper(Byte.parseByte(cmd.getOptionValue("mapper")));
            }

            if(cmd.hasOption("mirror")){
                builder.setMirror(Byte.parseByte(cmd.getOptionValue("mirror")));
            }

            if(cmd.hasOption("backup")){
                builder.setBackup(Byte.parseByte(cmd.getOptionValue("backup")));
            }

            if(cmd.hasOption("prgbanks")){
                builder.setPrgBanks(Byte.parseByte(cmd.getOptionValue("prgbanks")));
            }

            if(cmd.hasOption("chrbanks")){
                builder.setCharBanks(Byte.parseByte(cmd.getOptionValue("chrbanks")));
            }

            // Assembly output
            Path pathOut = cmd.hasOption("output") ? Path.of(cmd.getOptionValue("output")) : null;
            File fileOut = pathOut == null ? new File(fileOutName) : new File(pathOut.toString() + File.separator + fileOutName);
            builder.setFileOut(fileOut);  

        } catch (ParseException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        return builder;
    }
    
    private static Options createOptions() {
        Option help = new Option("h", "help", false, "print this message");
        Option cfg = new Option("cfg", "config", true, "ROM Configuration file");
        Option input = new Option("i", "input", true, "Place the output into <file>");
        Option output = new Option("o", "output", true, "Place the output into <file>");
        Option mapper = new Option("ma", "mapper", true, "iNES mapper");
        Option mirror = new Option("mi", "mirror", true, "Nametable mirroring, 1 = Vertical, 0 = Horizontal");
        Option backup = new Option("b", "backup", true, "Enable save backup support");
        Option prgbanks = new Option("prg", "prgbanks", true, "Number of 16k prg banks");
        Option chrbanks = new Option("chr", "chrbanks", true, "Number of 8k prg banks");

        return new Options().addOption(cfg)
                            .addOption(help)
                            .addOption(input)
                            .addOption(output)
                            .addOption(mapper)
                            .addOption(mirror)
                            .addOption(backup)
                            .addOption(prgbanks)
                            .addOption(chrbanks);
    }

    public static void main(String args[]) throws IOException {
        CompilerBuilder builder = parseArguments(args);
        try {
            Compiler compiler = builder.toCompiler();
            compiler.compile();
        } catch (Throwable e) {
            System.err.println("\t" + e.getMessage());
            System.exit(1);
        }
    }
}
