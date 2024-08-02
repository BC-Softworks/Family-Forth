package com.famiforth.generator;

import java.io.IOException;
import java.util.List;

import com.famiforth.parser.ParserToken;

public interface Generator {

    void writeFileHeader(List<String> headerFileList) throws IOException;

    void writeFileHeader() throws IOException;

    void generate(ParserToken token) throws IOException;

    void close() throws IOException;

}