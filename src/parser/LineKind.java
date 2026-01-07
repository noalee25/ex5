package parser;

public enum LineKind {
    EMPTY,
    COMMENT,
    METHOD_DECLARATION,
    IF_WHILE_HEADER,
    CLOSE_BRACE,
    RETURN,
    VAR_DECLARATION,
    ASSIGNMENT,
    METHOD_CALL
}
