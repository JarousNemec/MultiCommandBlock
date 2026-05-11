package org.jardathedev.multicommandblock.shared.enums;

public enum ExecutionMode {
    FAST("fast"),
    TICK("tick");

    private final String label;

    ExecutionMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
