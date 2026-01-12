package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method in s-Java.
 * All methods are void and have a name and list of parameters.
 */
public class Method {
    private final String name;
    private final List<Variable> parameters;
    private final int declarationLine;

    /**
     * constructor
     * @param name method name
     * @param declarationLine line number of declaration of the method
     */
    public Method(String name, int declarationLine) {
        this.name = name;
        this.parameters = new ArrayList<>();
        this.declarationLine = declarationLine;
    }

    /**
     * @return list of all the parameters in the method
     */
    public List<Variable> getParameters() {
        return parameters;
    }

    /**
     * adds a parameter to the function
     * @param param
     */
    public void addParameter(Variable param) {
        parameters.add(param);
    }

    /**
     * Checks if the given argument types match this method's parameter types
     * Accounts for type compatibility
     * @param argTypes List of argument type names
     * @return true if arguments match, false otherwise
     */
    public boolean matchesArgumentTypes(List<String> argTypes) {
        if (argTypes.size() != parameters.size()) {
            return false;
        }
        for (int i = 0; i < parameters.size(); i++) {
            Variable param = parameters.get(i);
            String argType = argTypes.get(i);
            
            if (!param.isCompatibleWith(argType)) {
                return false;
            }
        }
        return true;
    }
}
