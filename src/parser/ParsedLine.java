package parser;

public class ParsedLine {
    public final int lineNumber;
    public final LineKind lineKind;
    public final String rawLine;
    private String cleanLine;

    public ParsedLine(int lineNumber, LineKind lineKind, String rawLine) {
        this.lineNumber = lineNumber;
        this.lineKind = lineKind;
        this.rawLine = rawLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public LineKind getLineKind() {
        return lineKind;
    }

    public String getRawLine() {
        return rawLine;
    }

}
