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

    public Method(String name, int declarationLine) {
        this.name = name;
        this.parameters = new ArrayList<>();
        this.declarationLine = declarationLine;
    }

    public String getName() {
        return name;
    }

    public List<Variable> getParameters() {
        return parameters;
    }

    public int getDeclarationLine() {
        return declarationLine;
    }

    public void addParameter(Variable param) {
        parameters.add(param);
    }

    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Checks if the given argument types match this method's parameter types.
     * Accounts for type compatibility (int→double, int/double→boolean).
     * 
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
