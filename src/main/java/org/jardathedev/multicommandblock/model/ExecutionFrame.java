package org.jardathedev.multicommandblock.model;

public class ExecutionFrame {
    public int enterIndex = -1;
    public int childIndentLevel = -1;
    public int startIndex = -1;
    public int endIndex = -1;
    public int remainingRevolutions = -1;
    public int revolutionsCount = -1;

    public ExecutionFrame copy() {
        ExecutionFrame f = new ExecutionFrame();
        f.enterIndex = this.enterIndex;
        f.startIndex = this.startIndex;
        f.endIndex = this.endIndex;
        f.childIndentLevel = this.childIndentLevel;
        f.revolutionsCount = this.revolutionsCount;
        f.remainingRevolutions = this.revolutionsCount;
        return f;
    }
}
