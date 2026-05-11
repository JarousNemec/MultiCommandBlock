package org.jardathedev.multicommandblock.shared.enums;

public enum ExecutionMode {
    FAST("fast", 0),
    TICK("tick", 1);

    private final String label;
    private final int numericValue;

    ExecutionMode(String label, int numericValue) {
        this.label = label;
        this.numericValue = numericValue;
    }

    public String getLabel() {
        return label;
    }

    public int getNumericValue() {
        return numericValue;
    }

}
