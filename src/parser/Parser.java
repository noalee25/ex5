package parser;

import model.Scope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {

    private final File file;
    private Stack<Scope> scopes;

    public Parser(File file) {
        this.file = file;
        scopes = new Stack<>();
    }

    public List<ParsedLine> parseFile() throws Exception {
        List<ParsedLine> lines = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int lineNumber = 0;

        //global scope
        scopes.push(new Scope(null));
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            //clean line from spaces and stuff...
            //create Parsed line and keep
        }
        //classify line

        return lines;
    }


    public ParsedLine parseLine(String rawLine, int lineNum) throws ParserException {
        //check if empty
        if (rawLine.trim().isEmpty()) {
            return new ParsedLine(lineNum, LineKind.EMPTY, rawLine);
        }
        //comment line
        if (rawLine.startsWith("//")) {
            return new ParsedLine(lineNum, LineKind.COMMENT, rawLine);
        }
        //multi-line comments
        if (rawLine.contains("/*") || rawLine.contains("*/")) {
            throw new ParserException(lineNum, rawLine);
        }
        //closing brace (must be alone)
        if (RegexBank.CLOSE_BRACE.matcher(rawLine).matches())
            return new ParsedLine(lineNum, LineKind.CLOSE_BRACE, rawLine);
        //end with ";" or "}"
        String trimmedLine = rawLine.trim();
        boolean endWithSemicolon = trimmedLine.endsWith(";");
        boolean endWithBrace = trimmedLine.endsWith("{");
        if (!endWithBrace && !endWithSemicolon) {
            throw new ParserException(lineNum, trimmedLine);
        }
        if (endWithSemicolon) {
            //return;
            if (RegexBank.RETURN_STMT.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.RETURN, rawLine);
            }
            // variable declaration: [final] type ...
            if (RegexBank.VAR_DECL_LINE.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.VAR_DECLARATION, rawLine);
            }
            // method call: name(args);
            if (RegexBank.METHOD_CALL.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.METHOD_CALL, rawLine);
            }
            // assignment(s): a=..., b=...;
            if (RegexBank.ONE_ASSIGNMENT_TOKEN.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.ASSIGNMENT, rawLine);
            }

            // If we got here: it ends with ';' but isn't recognized
            throw new ParserException(lineNum, "Unrecognized statement: " + rawLine);
        } else {
            //if(condition) { / while(condition) {
            if (RegexBank.IF_WHILE_HEADER.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.IF_WHILE_HEADER, rawLine);
            }
            //method declaration - void name(params) {
            if (RegexBank.METHOD_DECL.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.METHOD_DECLARATION, rawLine);
            }
        }
        return null;
    }
}
