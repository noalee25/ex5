package parser;

import java.util.regex.Pattern;

public class RegexBank {
    private RegexBank() {} //no need in instances

    //close brace
    public static final Pattern CLOSE_BRACE =
            Pattern.compile("^\\s*}\\s*$");

    //return
    public static final Pattern RETURN_STMT =
            Pattern.compile("^\\s*return\\s*;\\s*$");

    //method declaration - must start with a letter + void + param may be empty
    public static final Pattern METHOD_DECL =
            Pattern.compile("^\\s*void\\s+([A-Za-z][A-Za-z0-9_]*)\\s*\\((.*)\\)\\s*\\{\\s*$");

    //if/while - starts with if/while + (condition) + open brace
    public static final Pattern IF_WHILE_HEADER =
            Pattern.compile("^\\s*(if|while)\\s*\\((.*)\\)\\s*\\{\\s*$");

    //declaration - final (optional) + type + declaration list string
    public static final Pattern VAR_DECL_LINE =
            Pattern.compile("^\\s*(final\\s+)?(int|double|boolean|char|String)\\s+(.+)\\s*;\\s*$");

    //method call - name + params may be empty
    public static final Pattern METHOD_CALL =
            Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\((.*)\\)\\s*;\\s*$");

    //param inside method declaration - final(opt) + type + name
    public static final Pattern PARAM_TOKEN =
            Pattern.compile("^\\s*(final\\s+)?(int|double|boolean|char|String)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*$");

    //one variable declaration in a list - name + = + value(optional)
    public static final Pattern ONE_VAR_DECL_TOKEN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*(?:=\\s*(.+))?\\s*$");

    //one assignment inside list - name + = + value
    public static final Pattern ONE_ASSIGNMENT_TOKEN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)\\s*$");

    //one assignment line - name + value + ","
    public static final Pattern ASSIGNMENT_LINE =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*\\s*=\\s*[^,;]+)(\\s*,\\s*[A-Za-z_][A-Za-z0-9_]*\\s*=\\s*[^,;]+)*\\s*;\\s*$");

    public static final Pattern INT_LITERAL =
            Pattern.compile("^[+-]?\\d+$");

    public static final Pattern DOUBLE_LITERAL =
            Pattern.compile("^[+-]?(?:\\d+\\.\\d+|\\d+\\.|\\.\\d+|\\d+)$");

    public static final Pattern BOOLEAN_LITERAL =
            Pattern.compile("^(true|false)$");

    //char literal - exactly one character between single quotes (no escape support in spec)
    public static final Pattern CHAR_LITERAL =
            Pattern.compile("^'[^']'$");

    //string literal - anything except quotes inside (no escape support)
    public static final Pattern STRING_LITERAL =
            Pattern.compile("^\"[^\"]*\"$");

    //valid identifier in Sjava
    public static final Pattern IDENTIFIER =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

}
