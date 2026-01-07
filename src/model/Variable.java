package model;

/**
 * Represents a variable in s-Java (global or local).
 * Tracks name, type, final modifier, and initialization state.
 */
public class Variable {
    private final String name;
    private final String type;  // int, double, boolean, char, String
    private final boolean isFinal;
    private boolean isInitialized;  // NOT final - can change during validation

    public Variable(String name, String type, boolean isFinal, boolean isInitialized) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.isInitialized = isInitialized;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Mark this variable as initialized (e.g., after assignment).
     */
    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    /**
     * Checks if this variable can accept a value of the given type.
     * 
     * Type compatibility rules:
     * - Same type is always compatible
     * - int can be assigned to double
     * - int and double can be assigned to boolean
     * 
     * @param otherType The type to check compatibility with
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(String otherType) {
        // Same type - always compatible
        if (this.type.equals(otherType)) {
            return true;
        }
        
        // double can accept int
        if (this.type.equals("double") && otherType.equals("int")) {
            return true;
        }
        
        // boolean can accept int or double
        if (this.type.equals("boolean") && 
            (otherType.equals("int") || otherType.equals("double"))) {
            return true;
        }
        
        return false;
    }
}
