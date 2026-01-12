package parser;

/**
 * keeps the line kind, raw line and line number
 */
public class ParsedLine {
    public final int lineNumber;
    public final LineKind lineKind;
    public final String rawLine;

    /**
     * constructor
     * @param lineNumber number of line in the file
     * @param lineKind type of line
     * @param rawLine the original line
     */
    public ParsedLine(int lineNumber, LineKind lineKind, String rawLine) {
        this.lineNumber = lineNumber;
        this.lineKind = lineKind;
        this.rawLine = rawLine;
    }

    /**
     * @return line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return line type
     */
    public LineKind getLineKind() {
        return lineKind;
    }

    /**
     * @return raw line
     */
    public String getRawLine() {
        return rawLine;
    }
}
