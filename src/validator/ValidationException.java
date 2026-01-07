package validator;

/**
 * Exception thrown when semantic validation fails.
 * This represents type 1 errors (illegal code).
 */
public class ValidationException extends Exception {
    private final int lineNumber;

    public ValidationException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    public ValidationException(String message) {
        super(message);
        this.lineNumber = -1;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}

