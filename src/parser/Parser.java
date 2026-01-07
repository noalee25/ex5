package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final File file;

    public Parser(File file) {
        this.file = file;
    }

    /**
     * Parses the entire file line by line.
     * @return List of ParsedLine objects representing each line
     * @throws IOException if file reading fails
     * @throws ParserException if syntax errors are found
     */
    public List<ParsedLine> parseFile() throws IOException, ParserException {
        List<ParsedLine> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Parse and classify this line
                ParsedLine parsedLine = parseLine(line, lineNumber);
                lines.add(parsedLine);
            }
        }

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
        //comment not at start + multi-line comments
        int idx = rawLine.indexOf("//");
        if (rawLine.contains("/*") || rawLine.contains("*/")
                || rawLine.contains("/**") || (idx >= 0 && idx != 0)) {
            throw new ParserException(lineNum, rawLine);
        }
        //closing brace (must be alone)
        if (RegexBank.CLOSE_BRACE.matcher(rawLine).matches())
            return new ParsedLine(lineNum, LineKind.CLOSE_BRACE, rawLine);
        //end with ";" or "{"
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
            //variable declaration - [final] type ...
            if (RegexBank.VAR_DECL_LINE.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.VAR_DECLARATION, rawLine);
            }
            //method call - name(args);
            if (RegexBank.METHOD_CALL.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.METHOD_CALL, rawLine);
            }
            //assignment(s) - a=..., b=...;
            if(trimmedLine.contains("=") && !RegexBank.VAR_DECL_LINE.matcher(rawLine).matches()) {
                String body = trimmedLine.substring(0, trimmedLine.length()-1);
                String[] parts = body.split(",");
                for(String part : parts) {
                    String token = part.trim();
                    if(token.isEmpty() || !RegexBank.ONE_ASSIGNMENT_TOKEN.matcher(token).matches()) {
                        throw new ParserException(lineNum, rawLine);
                    }
                }
                return new ParsedLine(lineNum, LineKind.ASSIGNMENT, rawLine);
            }

            //if we got here - it ends with ';' but isn't recognized
            throw new ParserException(lineNum, "Unrecognized statement: " + rawLine);
        } else {
            //method declaration - void name(params) {
            if (RegexBank.METHOD_DECL.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.METHOD_DECLARATION, rawLine);
            }
            //if(condition) { / while(condition) {
            if (RegexBank.IF_WHILE_HEADER.matcher(rawLine).matches()) {
                return new ParsedLine(lineNum, LineKind.IF_WHILE_HEADER, rawLine);
            }

        }
        throw new ParserException(lineNum, "Unrecognized statement");
    }
}
