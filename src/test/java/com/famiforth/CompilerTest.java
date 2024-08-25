package com.famiforth;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.famiforth.compiler.Compiler;

public class CompilerTest {
    
    private Compiler compiler;

    private File fileIn;
    private File fileOut;


    @Test
    public void compileCoreTest() throws IOException {
        fileIn = new File("src/main/forth/core.f");
        fileOut = new File("build/asm/core.asm");
        compiler = new Compiler(fileIn, fileOut, null);
        compiler.compile();
    }

    @Test
    public void compileMathTest() throws IOException {
        fileIn = new File("src/main/forth/math.f");
        fileOut = new File("build/asm/math.asm");
        compiler = new Compiler(fileIn, fileOut, null);
        compiler.compile();
    }

    @Test
    public void compileMemoryTest() throws IOException {
        fileIn = new File("src/main/forth/memory.f");
        fileOut = new File("build/asm/memory.asm");
        compiler = new Compiler(fileIn, fileOut, null);
        compiler.compile();
    }

    @Test
    public void compileCoreExtTest() throws IOException {
        fileIn = new File("src/main/forth/core_ext.f");
        fileOut = new File("build/asm/core_ext.asm");
        compiler = new Compiler(fileIn, fileOut, null);
        compiler.compile();
    }
}
