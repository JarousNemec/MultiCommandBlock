package org.jardathedev.multicommandblock.entity.programProcessor;

import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.shared.*;
import org.jardathedev.multicommandblock.shared.enums.LineState;
import org.jardathedev.multicommandblock.shared.enums.LineType;

import java.util.ArrayList;
import java.util.List;

public class ProgramManager {
    private final List<String> rawLines;

    private final ProgramData program;
    private final ProgramRuntime runtime;
    private final CmdSourceStore cmdSourceStore;

    public ProgramManager() {
        this.rawLines = new ArrayList<>();
        this.cmdSourceStore = new CmdSourceStore();
        this.program = new ProgramData();
        this.runtime = new ProgramRuntime(program, cmdSourceStore);
    }

    public void start() {
        runtime.start();
    }

    public void stop() {
        runtime.stop();
    }

    public void programTick(BlockEntityAttributes attrs) {
        //check if compiled
        if (!program.isCompiled()) {
            Multicommandblock.LOGGER.info("Compiling before execution...");
            ProgramCompiler.compileProgram(rawLines, program, attrs, cmdSourceStore);
            Multicommandblock.LOGGER.info("Compilation complete!");
        }
        if (program.isExecutable() && program.isExecuting())
            runtime.programTick(attrs);
    }

    public void setNewLines(List<String> newLines) {
        this.rawLines.clear();
        this.rawLines.addAll(newLines);
        program.setCompilationResults(false, false);
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    public List<Integer> getInvalidLines() {
        List<Integer> invalidLines = new ArrayList<>();
        for (int i = 0; i < program.getSize(); i++) {
            if (!program.getLine(i).isValid()) {
                invalidLines.add(i);
            }
        }
        return invalidLines;
    }

    public void setClientRawLines(List<String> newLines, List<Integer> invalidLines) {
        program.clear();
        rawLines.clear();
        rawLines.addAll(newLines);

        var programLines = program.getProgramLines();

        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            LineState lineState = invalidLines.contains(i) ? LineState.INVALID : LineState.VALID;
            programLines.add(new CommandLine(line, LineType.RAW, lineState, -1, false, false));
        }
    }
}
