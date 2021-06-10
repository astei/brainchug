package me.steinborn.brainchug.reader;

import me.steinborn.brainchug.BrainfuckKeyword;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a lexer for the Brainfuck language. This lexer is trivial to implement as Brainfuck contains only 8
 * keywords, all of which are a single character in size, making translation trivial.
 */
public class BrainfuckLexer {
    private BrainfuckLexer() {
        throw new AssertionError();
    }

    /**
     * Tokenizes the entire Brainfuck program.
     * @param reader the source to read from
     * @return a list of all keywords in the program
     * @throws IOException if an I/O error is raised
     */
    public static List<BrainfuckKeyword> lex(Reader reader) throws IOException {
        List<BrainfuckKeyword> keywords = new ArrayList<>();
        int in;
        while ((in = reader.read()) != -1) {
            if (in == '>') {
                keywords.add(BrainfuckKeyword.INCREMENT_PTR);
            } else if (in == '<') {
                keywords.add(BrainfuckKeyword.DECREMENT_PTR);
            } else if (in == '+') {
                keywords.add(BrainfuckKeyword.INCREMENT_VAL);
            } else if (in == '-') {
                keywords.add(BrainfuckKeyword.DECREMENT_VAL);
            } else if (in == '.') {
                keywords.add(BrainfuckKeyword.PRINT_VAL);
            } else if (in == ',') {
                keywords.add(BrainfuckKeyword.INPUT_VAL);
            } else if (in == '[') {
                keywords.add(BrainfuckKeyword.LOOP_BEGIN);
            } else if (in == ']') {
                keywords.add(BrainfuckKeyword.LOOP_END);
            } else if (in == '#') {
                keywords.add(BrainfuckKeyword.DEBUG);
            }
        }
        return keywords;
    }
}
