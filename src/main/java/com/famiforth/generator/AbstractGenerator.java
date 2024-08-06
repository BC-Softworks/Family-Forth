package com.famiforth.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.famiforth.parser.ParserToken;

public abstract class AbstractGenerator {
    protected static final String lineSeparator = System.lineSeparator();
    private FileOutputStream fileOutputStream;

    public AbstractGenerator(FileOutputStream fileOutputStream){
        this.fileOutputStream = fileOutputStream;
    }

    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }
    
    public void close() throws IOException{
        fileOutputStream.close();
    };

    public abstract void writeFileHeader() throws IOException;

    public abstract List<String> generate(ParserToken token) throws IOException;

}