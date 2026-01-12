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

    /**
     * constructor
     * @param name variable name
     * @param type of the var
     * @param isFinal true if final var, false else
     * @param isInitialized true if was initialized with a value, false else
     */
    public Variable(String name, String type, boolean isFinal, boolean isInitialized) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.isInitialized = isInitialized;
    }

    /**
     * @return name of variable
     */
    public String getName() {
        return name;
    }

    /**
     * @return type of the variable
     */
    public String getType() {
        return type;
    }

    /**
     * @return boolean according to final var or not
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * @return boolean according to initialized var or not
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * used to mark this variable as initialized (after assignment)
     */
    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    /**
     * Checks if this variable can accept a value of the given type
     * @param otherType The type to check compatibility with
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(String otherType) {
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
