package model;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;
    private Map<String, Variable> variables = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

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
