package validator;

import model.Method;
import model.Scope;
import model.Variable;
import parser.LineKind;
import parser.ParsedLine;
import parser.RegexBank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * validates the code after parsing
 * Pass 1: Collect all globals and method signatures
 * Pass 2: Validate method bodies and semantics
 */
public class Validator {
    
    private final List<ParsedLine> lines;
    private Scope globalScope;
    private Map<String, Method> methods;

    /**
     * constructor
     * @param lines list of all the ParsedLines
     */
    public Validator(List<ParsedLine> lines) {
        this.lines = lines;
        this.globalScope = new Scope(null);
        this.methods = new HashMap<>();
    }
    
    /**
     * main validation method (runs both passes)
     */
    public void validate() throws ValidationException {
        // Pass 1: collect data from code
        collectGlobalsAndMethods();
        // Pass 2: validate data
        validateMethodBodies();
    }
    
    //PASS 1: COLLECTION OF DATA
    /**
     * scan all lines and collect variables and methods (validates structural rules)
     */
    private void collectGlobalsAndMethods() throws ValidationException {
        int i = 0;
        while (i < lines.size()) {
            ParsedLine line = lines.get(i);
            switch (line.getLineKind()) {
                case VAR_DECLARATION:
                    // global variable declaration
                    processGlobalVarDeclaration(line);
                    i++;
                    break;
                    
                case METHOD_DECLARATION:
                    // method declaration
                    processMethodDeclaration(line);
                    // after process skip to end of method
                    i = skipToMethodEnd(i);
                    break;
                    
                case EMPTY: //does nothing

                case COMMENT:
                    i++;
                    break;
                    
                case METHOD_CALL:
                    throw new ValidationException(line.getLineNumber(), 
                        "Method calls not allowed in global scope");
                    
                case ASSIGNMENT:
                    processGlobalAssignment(line);
                    i++;
                    break;

                case IF_WHILE_HEADER:

                case RETURN:
                    throw new ValidationException(line.getLineNumber(),
                        "Statement only allowed inside methods");
                    
                case CLOSE_BRACE:
                    throw new ValidationException(line.getLineNumber(),
                        "Unexpected closing brace in global scope");
                    
                default:
                    i++;
            }
        }
    }

    
    /**
     * Process a global variable declaration.
     */
    private void processGlobalVarDeclaration(ParsedLine line) throws ValidationException {
        Matcher m = RegexBank.VAR_DECL_LINE.matcher(line.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(line.getLineNumber(), "Invalid variable declaration");
        }
        
        String finalModifier = m.group(1); //checks if final var
        boolean isFinal = (finalModifier != null && finalModifier.trim().equals("final"));
        String type = m.group(2);
        String declarationsStr = m.group(3);
        
        // split by comma and process each variable
        String[] declarations = declarationsStr.split(",");
        for (String decl : declarations) {
            decl = decl.trim();
            Matcher declMatcher = RegexBank.ONE_VAR_DECL_TOKEN.matcher(decl);
            if (!declMatcher.matches()) {
                throw new ValidationException(line.getLineNumber(), 
                    "Invalid variable declaration: " + decl);
            }
            String varName = declMatcher.group(1);
            String valueStr = declMatcher.group(2);
            // validate variable name
            if (!isValidVariableName(varName)) {
                throw new ValidationException(line.getLineNumber(), 
                    "Invalid variable name: " + varName);
            }
            // check for duplicate global variable
            if (globalScope.resolve(varName) != null) {
                throw new ValidationException(line.getLineNumber(), 
                    "Variable " + varName + " already declared in global scope");
            }
            // check final variables must be initialized
            if (isFinal && valueStr == null) {
                throw new ValidationException(line.getLineNumber(), 
                    "Final variable " + varName + " must be initialized");
            }
            // create and add variable + check type value
            boolean isInitialized = (valueStr != null);
            if (isInitialized) {
                // check the type match the value
                String valueType = determineValueType(valueStr, globalScope, line.getLineNumber());
                Variable tempVar = new Variable(varName, type, false, false);
                if (!tempVar.isCompatibleWith(valueType)) {
                    throw new ValidationException(line.getLineNumber(),
                            "Cannot assign " + valueType + " to " + type);
                }
            }
            Variable var = new Variable(varName, type, isFinal, isInitialized);
            globalScope.addVariable(var);
        }
    }

    /**
     * Process a global assignment statement.
     */
    private void processGlobalAssignment(ParsedLine line) throws ValidationException {
        String content = line.getRawLine().trim();
        if (content.endsWith(";")) {
            content = content.substring(0, content.length() - 1);
        }
        String[] assignments = content.split(",");
        for (String assignment : assignments) {
            assignment = assignment.trim();
            Matcher m = RegexBank.ONE_ASSIGNMENT_TOKEN.matcher(assignment);
            if (!m.matches()) {
                throw new ValidationException(line.getLineNumber(),
                        "Invalid assignment: " + assignment);
            }
            String varName = m.group(1);
            String valueStr = m.group(2).trim();
            Variable var = globalScope.resolve(varName);
            if (var == null) {
                throw new ValidationException(line.getLineNumber(),
                        "Variable " + varName + " not declared");
            }
            if (var.isFinal()) {
                throw new ValidationException(line.getLineNumber(),
                        "Cannot assign to final variable " + varName);
            }
            String valueType = determineValueType(valueStr, globalScope, line.getLineNumber());
            if (!var.isCompatibleWith(valueType)) {
                throw new ValidationException(line.getLineNumber(),
                        "Cannot assign " + valueType + " to " + var.getType());
            }
            var.setInitialized(true);
        }
    }
    
    /**
     * Process a method declaration and collect its signature.
     */
    private void processMethodDeclaration(ParsedLine line) throws ValidationException {
        Matcher m = RegexBank.METHOD_DECL.matcher(line.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(line.getLineNumber(), "Invalid method declaration");
        }
        String methodName = m.group(1);
        String paramsStr = m.group(2);
        // Check for duplicate method
        if (methods.containsKey(methodName)) {
            throw new ValidationException(line.getLineNumber(), 
                "Method " + methodName + " already declared");
        }
        Method method = new Method(methodName, line.getLineNumber());
        // Parse parameters
        if (paramsStr != null && !paramsStr.trim().isEmpty()) {
            String[] params = paramsStr.split(",");
            Set<String> paramNames = new HashSet<>();
            for (String param : params) {
                param = param.trim();
                Matcher paramMatcher = RegexBank.PARAM_TOKEN.matcher(param);
                if (!paramMatcher.matches()) {
                    throw new ValidationException(line.getLineNumber(), 
                        "Invalid parameter: " + param);
                }
                String finalMod = paramMatcher.group(1);
                boolean isFinal = (finalMod != null && finalMod.trim().equals("final"));
                String paramType = paramMatcher.group(2);
                String paramName = paramMatcher.group(3);
                // Validate parameter name
                if (!isValidVariableName(paramName)) {
                    throw new ValidationException(line.getLineNumber(), 
                        "Invalid parameter name: " + paramName);
                }
                // Check for duplicate parameter names
                if (paramNames.contains(paramName)) {
                    throw new ValidationException(line.getLineNumber(), 
                        "Duplicate parameter name: " + paramName);
                }
                paramNames.add(paramName);
                Variable paramVar = new Variable(paramName, paramType, isFinal, true);
                method.addParameter(paramVar);
            }
        }
        methods.put(methodName, method);
    }
    
    /**
     * Skip from method declaration to its closing brace.
     */
    private int skipToMethodEnd(int startIndex) {
        int braceCount = 1;
        int i = startIndex + 1;
        while (i < lines.size() && braceCount > 0) {
            ParsedLine line = lines.get(i);
            if ((line.getLineKind() == LineKind.METHOD_DECLARATION
                    && braceCount == 1 )||
                line.getLineKind() == LineKind.IF_WHILE_HEADER) {
                braceCount++;
            } else if (line.getLineKind() == LineKind.CLOSE_BRACE) {
                braceCount--;
            }
            i++;
        }
        return i;
    }
    
    //PASS 2: VALIDATION
    /**
     * validate all method bodies.
     */
    private void validateMethodBodies() throws ValidationException {
        int i = 0;
        while (i < lines.size()) {
            ParsedLine line = lines.get(i);
            if (line.getLineKind() == LineKind.METHOD_DECLARATION) {
                validateMethodBody(line, i);
                i = skipToMethodEnd(i);
            } else {
                i++;
            }
        }
    }
    
    /**
     * Validate a single method body.
     */
    private void validateMethodBody(ParsedLine methodLine, int startIndex) throws ValidationException {
        // Extract method name
        Matcher m = RegexBank.METHOD_DECL.matcher(methodLine.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(methodLine.getLineNumber(), "Invalid method declaration");
        }
        String methodName = m.group(1);
        Method method = methods.get(methodName);
        // Create method scope with parameters
        Scope methodScope = new Scope(globalScope);
        for (Variable param : method.getParameters()) {
            methodScope.addVariable(param);
        }
        Scope currentScope = methodScope;
        int i = startIndex + 1;
        int braceCount = 1;
        boolean hasReturn = false;
        ParsedLine lastCodeLine = null;
        while (i < lines.size() && braceCount > 0) {
            ParsedLine line = lines.get(i);
            // Track not empty and non comment lines
            if (line.getLineKind() != LineKind.EMPTY
                    && line.getLineKind() != LineKind.COMMENT
                    && line.getLineKind() != LineKind.CLOSE_BRACE) {
                lastCodeLine = line;
            }
            switch (line.getLineKind()) {
                case CLOSE_BRACE:
                    braceCount--;
                    if (braceCount == 0) {
                        // end of method - check return
                        if (!hasReturn ||
                                (lastCodeLine != null && lastCodeLine.getLineKind() != LineKind.RETURN)) {
                            throw new ValidationException(line.getLineNumber(), 
                                "Method must end with return statement");
                        }
                    } else {
                        // end of inner block - pop scope
                        currentScope = currentScope.getParent();
                    }
                    break;
                    
                case VAR_DECLARATION:
                    processLocalVarDeclaration(line, currentScope);
                    break;
                    
                case ASSIGNMENT:
                    processAssignment(line, currentScope);
                    break;
                    
                case METHOD_CALL:
                    processMethodCall(line, currentScope);
                    break;
                    
                case IF_WHILE_HEADER:
                    processCondition(line, currentScope);
                    braceCount++;
                    // create nested scope
                    currentScope = new Scope(currentScope);
                    break;
                    
                case RETURN:
                    hasReturn = true;
                    break;
                    
                case METHOD_DECLARATION:
                    throw new ValidationException(line.getLineNumber(), 
                        "Nested method declarations not allowed");
                    
                case EMPTY:
                case COMMENT:
                    break;
                    
                default:
                    break;
            }
            i++;
        }
    }
    
    /**
     * Process local variable declaration.
     */
    private void processLocalVarDeclaration(ParsedLine line, Scope scope) throws ValidationException {
        Matcher m = RegexBank.VAR_DECL_LINE.matcher(line.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(line.getLineNumber(), "Invalid variable declaration");
        }
        String finalModifier = m.group(1);
        boolean isFinal = (finalModifier != null && finalModifier.trim().equals("final"));
        String type = m.group(2);
        String declarationsStr = m.group(3);
        String[] declarations = declarationsStr.split(",");
        for (String decl : declarations) {
            decl = decl.trim();
            Matcher declMatcher = RegexBank.ONE_VAR_DECL_TOKEN.matcher(decl);
            if (!declMatcher.matches()) {
                throw new ValidationException(line.getLineNumber(), 
                    "Invalid variable declaration: " + decl);
            }
            String varName = declMatcher.group(1);
            String valueStr = declMatcher.group(2);
            if (!isValidVariableName(varName)) {
                throw new ValidationException(line.getLineNumber(), 
                    "Invalid variable name: " + varName);
            }
            // check for duplicate in current scope
            Variable existing = scope.resolve(varName);
            if (existing != null && scope.getParent() != null) {
                // variable exists - check if it is in parent scope or current scope
                Scope parentScope = scope.getParent();
                Variable inParent = (parentScope != null) ? parentScope.resolve(varName) : null;
                if (existing != inParent) {
                    throw new ValidationException(line.getLineNumber(),
                            "Variable " + varName + " already declared in this scope");
                }
            }
            if (isFinal && valueStr == null) {
                throw new ValidationException(line.getLineNumber(), 
                    "Final variable " + varName + " must be initialized");
            }
            boolean isInitialized = (valueStr != null);
            if (isInitialized) {
                String valueType = determineValueType(valueStr, scope, line.getLineNumber());
                Variable tempVar = new Variable(varName, type, false, false);
                if (!tempVar.isCompatibleWith(valueType)) {
                    throw new ValidationException(line.getLineNumber(), 
                        "Cannot assign " + valueType + " to " + type);
                }
            }
            Variable var = new Variable(varName, type, isFinal, isInitialized);
            scope.addVariable(var);
        }
    }
    
    /**
     * Process assignment statement.
     */
    private void processAssignment(ParsedLine line, Scope scope) throws ValidationException {
        String content = line.getRawLine().trim();
        if (content.endsWith(";")) {
            content = content.substring(0, content.length() - 1);
        }
        String[] assignments = content.split(",");
        for (String assignment : assignments) {
            assignment = assignment.trim();
            Matcher m = RegexBank.ONE_ASSIGNMENT_TOKEN.matcher(assignment);
            if (!m.matches()) {
                throw new ValidationException(line.getLineNumber(), 
                    "Invalid assignment: " + assignment);
            }
            String varName = m.group(1);
            String valueStr = m.group(2).trim();
            Variable var = scope.resolve(varName);
            if (var == null) {
                throw new ValidationException(line.getLineNumber(), 
                    "Variable " + varName + " not declared");
            }
            if (var.isFinal()) {
                throw new ValidationException(line.getLineNumber(), 
                    "Cannot assign to final variable " + varName);
            }
            String valueType = determineValueType(valueStr, scope, line.getLineNumber());
            if (!var.isCompatibleWith(valueType)) {
                throw new ValidationException(line.getLineNumber(), 
                    "Cannot assign " + valueType + " to " + var.getType());
            }
            if(scope.getVariables().containsKey(var.getName())) {
                var.setInitialized(true);
            }
        }
    }
    
    /**
     * Process method call.
     */
    private void processMethodCall(ParsedLine line, Scope scope) throws ValidationException {
        Matcher m = RegexBank.METHOD_CALL.matcher(line.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(line.getLineNumber(), "Invalid method call");
        }
        String methodName = m.group(1);
        String argsStr = m.group(2);
        Method method = methods.get(methodName);
        if (method == null) {
            throw new ValidationException(line.getLineNumber(), 
                "Method " + methodName + " not defined");
        }
        // parse arguments
        List<String> argTypes = new ArrayList<>();
        if (argsStr != null && !argsStr.trim().isEmpty()) {
            String[] args = argsStr.split(",");
            for (String arg : args) {
                arg = arg.trim();
                String argType = determineValueType(arg, scope, line.getLineNumber());
                argTypes.add(argType);
            }
        }
        if (!method.matchesArgumentTypes(argTypes)) {
            throw new ValidationException(line.getLineNumber(), 
                "Method " + methodName + " called with incompatible arguments");
        }
    }
    
    /**
     * Process and validate condition in if/while.
     */
    private void processCondition(ParsedLine line, Scope scope) throws ValidationException {
        Matcher m = RegexBank.IF_WHILE_HEADER.matcher(line.getRawLine());
        if (!m.matches()) {
            throw new ValidationException(line.getLineNumber(), "Invalid if/while statement");
        }
        String conditionStr = m.group(2).trim();
        validateCondition(conditionStr, scope, line.getLineNumber());
    }
    
    /**
     * Validate a boolean condition.
     */
    private void validateCondition(String conditionStr, Scope scope, int lineNumber) 
            throws ValidationException {
        // split by || and &&
        String[] orParts = conditionStr.split("\\|\\|");
        for (String orPart : orParts) {
            String[] andParts = orPart.split("&&");
            for (String element : andParts) {
                element = element.trim();
                if (element.isEmpty()) {
                    throw new ValidationException(lineNumber, "Invalid condition: empty element");
                }
                // check if boolean literal
                if (RegexBank.BOOLEAN_LITERAL.matcher(element).matches()) {
                    continue;
                }
                // check if number literal
                if (RegexBank.INT_LITERAL.matcher(element).matches() || 
                    RegexBank.DOUBLE_LITERAL.matcher(element).matches()) {
                    continue;
                }
                // check if variable
                if (RegexBank.IDENTIFIER.matcher(element).matches()) {
                    Variable var = scope.resolve(element);
                    if (var == null) {
                        throw new ValidationException(lineNumber, 
                            "Variable " + element + " not declared");
                    }
                    if (!var.isInitialized()) {
                        throw new ValidationException(lineNumber, 
                            "Variable " + element + " may not be initialized");
                    }
                    String type = var.getType();
                    if (!type.equals("boolean") && !type.equals("int") && !type.equals("double")) {
                        throw new ValidationException(lineNumber, 
                            "Condition must be boolean, int, or double");
                    }
                    continue;
                }
                throw new ValidationException(lineNumber, 
                    "Invalid condition element: " + element);
            }
        }
    }
    
    //HELPER METHODS
    /**
     * Determine the type of a value expression.
     */
    private String determineValueType(String valueStr, Scope scope, int lineNumber) 
            throws ValidationException {
        valueStr = valueStr.trim();
        // check literals
        if (RegexBank.INT_LITERAL.matcher(valueStr).matches()) {
            return "int";
        }
        if (RegexBank.DOUBLE_LITERAL.matcher(valueStr).matches()) {
            return "double";
        }
        if (RegexBank.BOOLEAN_LITERAL.matcher(valueStr).matches()) {
            return "boolean";
        }
        if (RegexBank.CHAR_LITERAL.matcher(valueStr).matches()) {
            return "char";
        }
        if (RegexBank.STRING_LITERAL.matcher(valueStr).matches()) {
            return "String";
        }
        // check if variable
        if (RegexBank.IDENTIFIER.matcher(valueStr).matches()) {
            Variable var = scope.resolve(valueStr);
            if (var == null) {
                throw new ValidationException(lineNumber, 
                    "Variable " + valueStr + " not declared");
            }
            if (!var.isInitialized()) {
                throw new ValidationException(lineNumber, 
                    "Variable " + valueStr + " may not be initialized");
            }
            return var.getType();
        }
        throw new ValidationException(lineNumber, "Invalid value: " + valueStr);
    }
    
    /**
     * Check if a variable name is valid according to s-Java rules.
     */
    private boolean isValidVariableName(String name) {
        // cannot start with digit
        if (name.length() > 0 && Character.isDigit(name.charAt(0))) {
            return false;
        }
        // cannot be just single underscore
        if (name.equals("_")) {
            return false;
        }
        // cannot start with double underscore
        if (name.startsWith("__")) {
            return false;
        }
        // must match general pattern
        return RegexBank.IDENTIFIER.matcher(name).matches();
    }
}

