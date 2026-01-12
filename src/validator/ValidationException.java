package validator;

/**
 * Exception thrown when semantic validation fails.
 * This represents type 1 errors (illegal code).
 */
public class ValidationException extends Exception {
    private final int lineNumber;

    /**
     * handling exceptions with massage and line number
     */
    public ValidationException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    /**
     * handling exceptions with massage
     */
    public ValidationException(String message) {
        super(message);
        this.lineNumber = -1;
    }
}

