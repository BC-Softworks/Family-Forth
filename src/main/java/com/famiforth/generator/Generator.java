package com.famiforth.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.famiforth.exceptions.GeneratorException;

public abstract class Generator {
    protected static final String lineSeparator = System.lineSeparator();
    private FileOutputStream fileOutputStream;

    public Generator(FileOutputStream fileOutputStream){
        this.fileOutputStream = fileOutputStream;
    }

    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }
    
    public void close() throws IOException{
        fileOutputStream.close();
    };

    /**
     * Write the list of strings in order
     * seperated by newlines to fileOutputStream
     * @param lines
     * @throws IOException
     */
    protected void writeLines(List<String> lines) {
        lines.forEach(line -> {
            try {
                getFileOutputStream().write((line).getBytes());
                getFileOutputStream().write((lineSeparator).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                throw new GeneratorException(e.getMessage());
            }
        });
    }
}
