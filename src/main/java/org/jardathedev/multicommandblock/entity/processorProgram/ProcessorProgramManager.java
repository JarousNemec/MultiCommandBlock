package org.jardathedev.multicommandblock.entity.processorProgram;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.shared.*;
import org.jardathedev.multicommandblock.shared.enums.LineState;
import org.jardathedev.multicommandblock.shared.enums.LineType;
import org.jardathedev.multicommandblock.util.CommandUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;

public class ProcessorProgramManager {
    private final List<CommandLine> programLines;
    private final List<ExecutionFrame> executionFrames;

    private final List<String> uncompiledLines;
    private final Deque<ExecutionFrame> executionStack;
    private boolean isCompiled = false;

    private int executionIndex = 0;
    private int sleepTicks = 0;
    private boolean isExecuting = false;

    private ServerCommandSource cachedSource;

    public ProcessorProgramManager() {
        this.programLines = new ArrayList<>();
        this.uncompiledLines = new ArrayList<>();
        this.executionFrames = new ArrayList<>();
        this.executionStack = new ArrayDeque<>();
    }

    public void setNewLines(List<String> uncompiledLines) {
        this.uncompiledLines.clear();
        this.uncompiledLines.addAll(uncompiledLines);
        isCompiled = false;
    }

    public void start() {
        isExecuting = true;
        executionIndex = 0;
        sleepTicks = 0;
    }

    public void stop() {
        isExecuting = false;
    }


    public void programTick(BlockEntityAttributes attrs) {
        //check if compiled
        if (!isCompiled) {
            Multicommandblock.LOGGER.info("Compiling before execution...");
            compileLines(uncompiledLines, attrs);
            Multicommandblock.LOGGER.info("Compilation complete!");
        }

        //escape if not running
        if (!isExecuting) {
            return;
        }

        // apply sleep
        if (sleepTicks > 0) {
            sleepTicks--;
            return;
        }

        // apply repeat
        while (!executionStack.isEmpty()) {
            ExecutionFrame frame = executionStack.peek();
            if (executionIndex > frame.endIndex) {
                frame.remainingRevolutions--;

                if (frame.remainingRevolutions > 0) {
                    executionIndex = frame.startIndex;
                    break;
                } else {
                    executionStack.pop();
                }

            } else {
                break;
            }
        }

        CommandLine line = programLines.get(executionIndex);

        //skip non executable lines
        while (!line.isExecutable()) {
            executionIndex++;
            line = programLines.get(executionIndex);
        }

        //execute one line
        executeLine(line, attrs);

        //increase line pointer
        executionIndex++;

        //stop run if end of program
        if (executionIndex < 0 || executionIndex >= programLines.size()) {
            Multicommandblock.LOGGER.info("Execution ended on index: {}", executionIndex - 1);
            isExecuting = false;
            executionIndex = 0;
            return;
        }
    }

    private void executeLine(CommandLine line, BlockEntityAttributes attrs) {
        MinecraftServer server = attrs.world().getServer();

        String command = line.commandBody();
        if (line.isMinecraft())
            executeMinecraftCommand(getSource(attrs), server.getCommandManager(), server, command);
        else if (line.isCustom())
            executeCustomCommand(command);

    }

    private void executeMinecraftCommand(ServerCommandSource source, CommandManager commandManager, MinecraftServer
            server, String command) {
        try {
            commandManager.executeWithPrefix(source.withSilent(), command);
        } catch (Exception e) {
            server.sendMessage(
                    Text.literal("§cCommandProcessor error: " + e.getMessage())
            );
        }
    }


    private void executeCustomCommand(String arguments) {
        List<String> args = parseArguments(arguments);
        if (args.size() <= 1)
            return;
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());

        if ("sleep".equals(command) && !params.isEmpty()) {
            sleepTicks = Math.max(0, Integer.parseInt(params.get(0)));
        } else if ("repeat".equals(command) && !params.isEmpty()) {
            ExecutionFrame frame = null;
            for (ExecutionFrame f : executionFrames) {
                if (f.enterIndex == executionIndex) {
                    frame = f.copy();
                    break;
                }
            }
            if (frame == null) {
                return;
            }
            if (frame.revolutionsCount <= 0) {
                executionIndex = frame.endIndex;
                return;
            }
            frame.remainingRevolutions = frame.revolutionsCount;
            executionStack.push(frame);
        }
    }

    private void compileLines(List<String> lines, BlockEntityAttributes attrs) {
        programLines.clear();
        executionFrames.clear();
        MinecraftServer server = attrs.world().getServer();

        int currentChildIndentLevel = 0;
        CommandLine programLine = null;
        int lastExecutableLineIndex = 0;
        for (int i = 0; i <= lines.size(); i++) {
            if (programLine != null) {
                programLines.add(programLine);
            }
            if (i == lines.size())
                break;

            String line = lines.get(i);
            String trimmed = line.stripLeading();
            int indent = line.length() - trimmed.length();
            int indentLevel = indent / INDENT.length();

            if (line.isBlank()) {
                programLine = new CommandLine(trimmed, LineType.BLANK, LineState.VALID, indentLevel, false, false);
                continue;
            }
            if (CommandUtil.isComment(trimmed)) {
                programLine = new CommandLine(trimmed, LineType.COMMENT, LineState.VALID, indentLevel, false, false);
                continue;
            }

            if (indentLevel < currentChildIndentLevel && programLine != null) {
                endCorrespondingFrames(programLines, executionFrames, indentLevel, lastExecutableLineIndex);
                currentChildIndentLevel = indentLevel;
            }

            if (indentLevel > currentChildIndentLevel) {
                programLine = new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
            } else if (trimmed.length() < 2 || (!trimmed.startsWith(MINECRAFT_COMMAND_START_CHAR) && !trimmed.startsWith(CUSTOM_COMMAND_START_CHAR))) {
                programLine = new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
            } else if (trimmed.startsWith(MINECRAFT_COMMAND_START_CHAR))
                if (isValidMinecraftCommand(trimmed.substring(1), getSource(attrs), server.getCommandManager())) {
                    programLine = new CommandLine(trimmed, LineType.MINECRAFT, LineState.VALID, indentLevel, true, false);
                    lastExecutableLineIndex = i;
                } else {
                    programLine = new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
                }
            else if (trimmed.startsWith(CUSTOM_COMMAND_START_CHAR)) {
                var result = isValidCustomCommand(trimmed.substring(1));
                if (result.isValid()) {
                    if (result.definesCodeBlock()) {
                        currentChildIndentLevel = indentLevel + 1;
                        if (i + 1 < lines.size()) {
                            var newFrame = new ExecutionFrame();
                            newFrame.childIndentLevel = currentChildIndentLevel;
                            newFrame.enterIndex = i;
                            newFrame.startIndex = i + 1;
                            newFrame.revolutionsCount = result.loopCount();
                            executionFrames.add(newFrame);
                        }
                        programLine = new CommandLine(trimmed, LineType.CUSTOM, LineState.VALID, indentLevel, true, true);
                    } else {
                        programLine = new CommandLine(trimmed, LineType.CUSTOM, LineState.VALID, indentLevel, true, false);
                    }
                    lastExecutableLineIndex = i;
                } else {
                    programLine = new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
                }
            } else
                programLine = new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
        }
        if (!programLines.isEmpty())
            endCorrespondingFrames(programLines, executionFrames, 0, lastExecutableLineIndex);
        isCompiled = true;
    }

    private boolean isValidMinecraftCommand(
            String command,
            ServerCommandSource source,
            CommandManager manager
    ) {
        var dispatcher = manager.getDispatcher();
        var parse = dispatcher.parse(command, source);

        if (parse.getContext().getNodes().isEmpty()) {
            return false;
        }

        if (parse.getReader().canRead()) {
            return false;
        }

        return true;
    }

    private CustomCommentValidationResult isValidCustomCommand(String line) {
        List<String> args = parseArguments(line);
        if (args.size() <= 1)
            return new CustomCommentValidationResult(false, false, 1);
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());
        if ("sleep".equals(command) && !params.isEmpty()) {
            if (!isInteger(params.get(0)))
                return new CustomCommentValidationResult(false, false, 1);

            int ticks;
            try {
                ticks = Integer.parseInt(params.get(0));
            } catch (NumberFormatException e) {
                return new CustomCommentValidationResult(false, false, 1);
            }

            return new CustomCommentValidationResult(ticks >= 0, false, 1);
        }
        if ("repeat".equals(command) && !params.isEmpty()) {
            if (!isInteger(params.get(0)))
                return new CustomCommentValidationResult(false, false, 1);
            int revolutions;
            try {
                revolutions = Integer.parseInt(params.get(0));
            } catch (NumberFormatException e) {
                return new CustomCommentValidationResult(false, false, 1);
            }
            return new CustomCommentValidationResult(revolutions >= 0, true, revolutions);
        }

        return new CustomCommentValidationResult(false, false, 1);
    }

    private ServerCommandSource getSource(BlockEntityAttributes attrs) {
        if (cachedSource == null || cachedSource.getWorld() != attrs.world()) {
            MinecraftServer server = attrs.world().getServer();
            cachedSource = new ServerCommandSource(
                    server,
                    Vec3d.ofCenter(attrs.pos()),
                    Vec2f.ZERO,
                    attrs.world(),
                    4,
                    "CommandProcessor",
                    Text.literal("CommandProcessor"),
                    server,
                    null
            );
        }
        return cachedSource;
    }

    public List<String> getRawLines() {
        return uncompiledLines;
    }


    public List<Integer> getInvalidLines() {
        List<Integer> invalidLines = new ArrayList<>();
        for (int i = 0; i < programLines.size(); i++) {
            if (!programLines.get(i).isValid()) {
                invalidLines.add(i);
            }
        }
        return invalidLines;
    }

    public void setClientRawLines(List<String> newLines, List<Integer> invalidLines) {
        programLines.clear();
        uncompiledLines.clear();
        uncompiledLines.addAll(newLines);
        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            LineState lineState = invalidLines.contains(i) ? LineState.INVALID : LineState.VALID;
            programLines.add(new CommandLine(line, LineType.RAW, lineState, -1, false, false));
        }
    }
}
