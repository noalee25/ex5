package model;

import java.util.HashMap;
import java.util.Map;

/**
 * keeps all relevant data for one given scope
 */
public class Scope {
    private final Scope parent;
    private Map<String, Variable> variables = new HashMap<>();

    /**
     * constructor
     * @param parent the parent scope
     */
    public Scope(Scope parent) {
        this.parent = parent;
    }

    /**
     * @return parent scope
     */
    public Scope getParent() {
        return parent;
    }

    /**
     * @return the map of the variables name as keys and the variables as values
     */
    public Map<String, Variable> getVariables() {
        return variables;
    }

    /**
     * update the variables map with the new var
     * @param variable add it to the variables map
     */
    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

    /**
     * resolves if a variable exists for this scope (including parents scope)
     * @param name of the variable
     * @return the variable if it exists already, null otherwise
     */
    public Variable resolve(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }
}
