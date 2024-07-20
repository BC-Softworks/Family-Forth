package com.famiforth;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java_cup.runtime.*;


public class Compiler
{
  Yylex l;
  parser p;

  public Compiler(String[] args)
  {
    try
    {
      Reader input = args.length == 0 ? new InputStreamReader(System.in) : new FileReader(args[0]);
      l = new Yylex(input);
    }
    catch(IOException e)
    {
      System.err.println("Error: File not found: " + args[0]);
      System.exit(1);
    }
    p = new parser(l, l.getSymbolFactory());
  }

  public com.famiforth.Absyn.EntryPoint parse() throws Exception
  {
    /* The default parser is the first-defined entry point. */
    com.famiforth.Absyn.EntryPoint ast = p.pPrg();
    System.out.println();
    System.out.println("Parse Successful!");
    System.out.println();
    System.out.println("[Abstract Syntax]");
    System.out.println();
    System.out.println(PrettyPrinter.show(ast));
    System.out.println();
    System.out.println("[Linearized Tree]");
    System.out.println();
    System.out.println(PrettyPrinter.print(ast));
    return ast;
  }

  public static void main(String args[]) throws Exception
  {
    Compiler comp = new Compiler(args);
    try
    {
      comp.parse();
    }
    catch(Throwable e)
    {
      System.err.println("At line " + String.valueOf(comp.l.line_num()) + ", near \"" + comp.l.buff() + "\" :");
      System.err.println("     " + e.getMessage());
      System.exit(1);
    }
  }
}
