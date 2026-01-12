package parser;

/**
 * throwing an exception caused by the parsed class (type 1)
 */
public class ParserException extends Exception {
    /**
     * handling exceptions with massage
     */
    public ParserException(String message) {
        super(message);
    }
    /**
     * handling exceptions with massage and kine number
     */
    public ParserException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
    }
}
