package parser;

public class ParserException extends Exception {
    public ParserException(String message) {
        super(message);
    }
    public ParserException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
    }
}
