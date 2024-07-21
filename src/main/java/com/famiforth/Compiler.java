package com.famiforth;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Compiler {
  Lexer scan;
  Parser parser;

  String defautlDictionaryLocation = "src/main/resources/base_dictionary.json";

  public Compiler(String[] args) {
    try {
      Reader input = args.length == 0 ? new InputStreamReader(System.in) : new FileReader(args[0]);
      scan = new Lexer(input);
    } catch(IOException e) {
      System.err.println("Error: File not found: " + args[0]);
      System.exit(1);
    }

    parser = new Parser(scan, defautlDictionaryLocation);
  }

  public void parse() throws Exception {
    parser.parse();
  }

  public static void main(String args[]) throws Exception {
    Compiler comp = new Compiler(args);
    try {
      comp.parse();
    } catch(Throwable e) {
      System.err.println("     " + e.getMessage());
      System.exit(1);
    }
  }
}
