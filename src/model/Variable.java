package model;

public class Variable {
    private final String name;
    private final String type;
    private final boolean isFinal;
    private final boolean isInitialized;

    public Variable(String name, String type, boolean isFinal, boolean isInitialized) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.isInitialized = isInitialized;
    }

    public String getName() {
        return name;
    }
}
