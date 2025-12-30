package org.jardathedev.multicommandblock.entity.processorProgram;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jardathedev.multicommandblock.Multicommandblock;
import org.jardathedev.multicommandblock.model.*;
import org.jardathedev.multicommandblock.util.CommandUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;

public class ProcessorProgramManager {
    private final List<CommandLine> _programLines;
    private final List<ExecutionFrame> _executionFrames;
    private final List<String> _uncompiledLines;
    private final Deque<ExecutionFrame> executionStack;
    private boolean _compiled = false;

    private int _executionIndex = 0;
    private int _sleepTicks = 0;
    private boolean _executing = false;

    private ServerCommandSource _cachedSource;

    public ProcessorProgramManager() {
        this._programLines = new ArrayList<>();
        this._uncompiledLines = new ArrayList<>();
        this._executionFrames = new ArrayList<>();
        this.executionStack = new ArrayDeque<>();
    }

    public void setNewLines(List<String> uncompiledLines) {
        _uncompiledLines.clear();
        _uncompiledLines.addAll(uncompiledLines);
        _compiled = false;
    }

    public void start() {
        _executing = true;
        _executionIndex = 0;
        _sleepTicks = 0;
    }

    public void stop() {
        _executing = false;
    }


    public void programTick(BlockEntityAttributes attrs) {
        if (!_compiled) {
            Multicommandblock.LOGGER.info("Compiling before execution...");
            compileLines(_uncompiledLines, attrs);
            Multicommandblock.LOGGER.info("Compilation complete!");
        }

        if (!_executing) {
            return;
        }

        // čekáme (sleep)
        if (_sleepTicks > 0) {
            Multicommandblock.LOGGER.info("Ticking sleep on index: {}", _executionIndex + 1);
            _sleepTicks--;
            return;
        }

        // aplikujeme repeat

        while (!executionStack.isEmpty()) {
            ExecutionFrame frame = executionStack.peek();
            Multicommandblock.LOGGER.info("Executing frame at index: {} with endIndex: {}", frame.enterIndex + 1, frame.endIndex + 1);
            if (_executionIndex > frame.endIndex) {
                frame.remainingRevolutions--;
                Multicommandblock.LOGGER.info("Remaining revolutions: {}", frame.remainingRevolutions);

                if (frame.remainingRevolutions > 0) {
                    _executionIndex = frame.startIndex;
                    Multicommandblock.LOGGER.info("Execution index: {}", _executionIndex + 1);
                    break;
                } else {
                    executionStack.pop();
                    Multicommandblock.LOGGER.info("Execution stack popped");
                }

            } else {
                break;
            }
        }


        if (_programLines.get(_executionIndex).isExecutable()) {
            Multicommandblock.LOGGER.info("Executing index: {}", _executionIndex + 1);
            executeProgramNextStep(attrs);
        } else
            Multicommandblock.LOGGER.info("Skipping index: {}", _executionIndex + 1);

        _executionIndex++;


        // konec
        if (_executionIndex < 0 || _executionIndex >= _programLines.size()) {
            Multicommandblock.LOGGER.info("Ending on index: {}", _executionIndex - 1);
            _executing = false;
            _executionIndex = 0;
            return;
        }
    }

    private void executeProgramNextStep(BlockEntityAttributes attrs) {
        MinecraftServer server = attrs.world().getServer();

        CommandLine line = _programLines.get(_executionIndex);

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
        if(args.size() <= 1)
            return;
        String command = args.get(0);
        List<String> params = args.subList(1, args.size());

        if ("sleep".equals(command) && !params.isEmpty()) {
            _sleepTicks = Math.max(0, Integer.parseInt(params.get(0)));
        } else if ("repeat".equals(command) && !params.isEmpty()) {
            ExecutionFrame frame = null;
            Multicommandblock.LOGGER.info("Executing repeat command...");
            for (ExecutionFrame f : _executionFrames) {
                if (f.enterIndex == _executionIndex) {
                    frame = f.copy();
                    break;
                }
            }
            if (frame == null) {
                Multicommandblock.LOGGER.info("Cannot find execution frame with enterIndex: {}", _executionIndex + 1);
                return;
            }
            if (frame.revolutionsCount <= 0) {
                _executionIndex = frame.endIndex;
                return;
            }
            frame.remainingRevolutions = frame.revolutionsCount;
            executionStack.push(frame);
            Multicommandblock.LOGGER.info("Successfully executed repeat command on index: {}", _executionIndex);
        }
    }

    private void compileLines(List<String> lines, BlockEntityAttributes attrs) {
        _programLines.clear();
        _executionFrames.clear();
        MinecraftServer server = attrs.world().getServer();

        int currentChildIndentLevel = 0;
        CommandLine programLine = null;
        int lastExecutableLineIndex = 0;
        for (int i = 0; i <= lines.size(); i++) {
            if (programLine != null) {
                Multicommandblock.LOGGER.info("lastExecutableIndex: {} indentLevel: {} lineNumber: {} state: {} type: {} commandBody: {}", lastExecutableLineIndex, programLine.indentLevel(), i - 1, programLine.state(), programLine.type(), programLine.commandBody());
                _programLines.add(programLine);
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
                endCorrespondingFrames(_programLines, _executionFrames, indentLevel, lastExecutableLineIndex);
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
                            _executionFrames.add(newFrame);
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
        if (!_programLines.isEmpty())
            endCorrespondingFrames(_programLines, _executionFrames, 0, lastExecutableLineIndex);
        _compiled = true;
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
        if(args.size() <= 1)
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
        if (_cachedSource == null || _cachedSource.getWorld() != attrs.world()) {
            MinecraftServer server = attrs.world().getServer();
            _cachedSource = new ServerCommandSource(
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
        return _cachedSource;
    }

    public List<String> getRawLines() {
        return _uncompiledLines;
    }


    public List<Integer> getInvalidLines() {
        List<Integer> invalidLines = new ArrayList<>();
        for (int i = 0; i < _programLines.size(); i++) {
            if (!_programLines.get(i).isValid()) {
                invalidLines.add(i);
            }
        }
        return invalidLines;
    }

    public void setClientRawLines(List<String> newLines, List<Integer> invalidLines) {
        _programLines.clear();
        _uncompiledLines.clear();
        _uncompiledLines.addAll(newLines);
        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            LineState lineState = invalidLines.contains(i) ? LineState.INVALID : LineState.VALID;
            _programLines.add(new CommandLine(line, LineType.RAW, lineState, -1, false, false));
        }
    }
}
