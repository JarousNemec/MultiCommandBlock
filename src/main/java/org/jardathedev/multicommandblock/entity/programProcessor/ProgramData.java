package org.jardathedev.multicommandblock.entity.programProcessor;

import org.jardathedev.multicommandblock.shared.CommandLine;
import org.jardathedev.multicommandblock.shared.ExecutionFrame;
import org.jardathedev.multicommandblock.shared.enums.ExecutionMode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ProgramData {
    private final List<CommandLine> programLines;
    private final List<ExecutionFrame> executionFrames;
    private final Deque<ExecutionFrame> executionStack;
    private boolean isCompiled = false;
    private boolean isExecutable = true;


    private int executionPointer = 0;
    private int sleepTicks = 0;
    private boolean isExecuting = false;
    private ExecutionMode executionMode = ExecutionMode.FAST;

    public ProgramData() {
        this.programLines = new ArrayList<>();
        this.executionFrames = new ArrayList<>();
        this.executionStack = new ArrayDeque<>();
    }

    public void clear() {
        this.programLines.clear();
        this.executionFrames.clear();
        this.executionStack.clear();
    }

    public void resetBeforeCompilation() {
        this.isCompiled = false;
        this.isExecutable = true;
        this.programLines.clear();
        this.executionFrames.clear();
        this.executionMode = ExecutionMode.FAST;
        this.sleepTicks = 0;
        this.executionPointer = 0;
    }

    public List<CommandLine> getProgramLines() {
        return programLines;
    }

    public List<ExecutionFrame> getExecutionFrames() {
        return executionFrames;
    }

    public Deque<ExecutionFrame> getExecutionStack() {
        return executionStack;
    }

    public int getSleepTicks() {
        return sleepTicks;
    }

    public int getExecutionPointer() {
        return executionPointer;
    }

    public CommandLine getPointerLine() {
        return programLines.get(executionPointer);
    }

    public CommandLine getLine(int pointer) {
        return programLines.get(pointer);
    }

    public int getSize() {
        return programLines.size();
    }

    public void setCompilationResults(boolean isCompiled, boolean isExecutable) {
        this.isCompiled = isCompiled;
        this.isExecutable = isExecutable;
    }

    public void setExecutionPointer(int executionPointer) {
        this.executionPointer = executionPointer;
    }

    public void setSleepTicks(int sleepTicks) {
        this.sleepTicks = sleepTicks;
    }

    public void setExecuting(boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    public boolean isCompiled() {
        return isCompiled;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void decreaseSleepTicksByOne() {
        sleepTicks--;
    }

    public void increaseExecutionPointerByOne() {
        executionPointer++;
    }

    public ExecutionMode getExecutionMode() {
        return this.executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }
}
