package org.jardathedev.multicommandblock.entity.programProcessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jardathedev.multicommandblock.shared.BlockEntityAttributes;
import org.jardathedev.multicommandblock.shared.CommandLine;
import org.jardathedev.multicommandblock.shared.CustomCommentValidationResult;
import org.jardathedev.multicommandblock.shared.ExecutionFrame;
import org.jardathedev.multicommandblock.shared.enums.LineState;
import org.jardathedev.multicommandblock.shared.enums.LineType;
import org.jardathedev.multicommandblock.util.CommandUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;
import static org.jardathedev.multicommandblock.util.CommandUtil.CUSTOM_COMMAND_START_CHAR;
import static org.jardathedev.multicommandblock.util.CommandUtil.MINECRAFT_COMMAND_START_CHAR;
import static org.jardathedev.multicommandblock.util.CommandUtil.endCorrespondingFrames;

public class ProgramCompiler {
public static void compileProgram(List<String> linesToCompile, ProgramData program, BlockEntityAttributes attrs, CmdSourceStore cmdSourceStore) {
        program.resetBeforeCompilation();

        var programLines = program.getProgramLines();
        var executionFrames = program.getExecutionFrames();

        MinecraftServer server = attrs.world().getServer();

        boolean hasErrors = false;
        int currentChildIndentLevel = 0;
        CommandLine programLine = null;
        int lastExecutableLineIndex = 0;

        for (int lineIndex = 0; lineIndex <= linesToCompile.size(); lineIndex++) {
            if (programLine != null) {
                programLines.add(programLine);
            }
            if (lineIndex == linesToCompile.size())
                break;

            String line = linesToCompile.get(lineIndex);
            String trimmed = line.stripLeading();
            int indent = line.length() - trimmed.length();
            int indentLevel = indent / INDENT.length();

            if (line.isBlank()) {
                programLine = createValidProgramLine(trimmed, LineType.BLANK, indentLevel, false, false);
                continue;
            }
            if (CommandUtil.isComment(trimmed)) {
                programLine = createValidProgramLine(trimmed, LineType.COMMENT, indentLevel, false, false);
                continue;
            }

            if (indentLevel < currentChildIndentLevel && programLine != null) {
                endCorrespondingFrames(programLines, executionFrames, indentLevel, lastExecutableLineIndex);
                currentChildIndentLevel = indentLevel;
            }

            if (indentLevel > currentChildIndentLevel) {
                programLine = createInvalidProgramLine(trimmed, indentLevel);
            } else if (trimmed.length() < 2 || (!trimmed.startsWith(MINECRAFT_COMMAND_START_CHAR) && !trimmed.startsWith(CUSTOM_COMMAND_START_CHAR))) {
                programLine = createInvalidProgramLine(trimmed, indentLevel);
            } else if (trimmed.startsWith(MINECRAFT_COMMAND_START_CHAR))
                if (isValidMinecraftCommand(trimmed.substring(1), cmdSourceStore.getSource(attrs), server.getCommandManager())) {
                    programLine = createValidProgramLine(trimmed, LineType.MINECRAFT, indentLevel, true, false);
                    lastExecutableLineIndex = lineIndex;
                } else {
                    programLine = createInvalidProgramLine(trimmed, indentLevel);
                }
            else if (trimmed.startsWith(CUSTOM_COMMAND_START_CHAR)) {
                var validationResult = isValidCustomCommand(trimmed.substring(1));
                if (validationResult.isValid()) {
                    if (validationResult.definesNewExecutionFrame()) {
                        currentChildIndentLevel = indentLevel + 1;
                        if (lineIndex + 1 < linesToCompile.size()) {
                            registerNewExecutionFrame(currentChildIndentLevel, lineIndex, validationResult, executionFrames);
                        }
                        programLine = createValidProgramLine(trimmed, LineType.CUSTOM, indentLevel, true, true);
                    } else {
                        programLine = createValidProgramLine(trimmed, LineType.CUSTOM, indentLevel, true, false);
                    }
                    lastExecutableLineIndex = lineIndex;
                } else {
                    programLine = createInvalidProgramLine(trimmed, indentLevel);
                }
            } else
                programLine = createInvalidProgramLine(trimmed, indentLevel);
        }
        if (!programLines.isEmpty())
            endCorrespondingFrames(programLines, executionFrames, 0, lastExecutableLineIndex);

        program.setCompilationResults(true, !hasErrors);
    }

    private static @NotNull CommandLine createValidProgramLine(String trimmed, LineType type, int indentLevel, boolean isExecutable, boolean hasBody) {
        return new CommandLine(trimmed, type, LineState.VALID, indentLevel, isExecutable, hasBody);
    }

    private static @NotNull CommandLine createInvalidProgramLine(String trimmed, int indentLevel) {
        return new CommandLine(trimmed, LineType.RAW, LineState.INVALID, indentLevel, false, false);
    }

    private static void registerNewExecutionFrame(int currentChildIndentLevel, int i, CustomCommentValidationResult result, List<ExecutionFrame> executionFrames) {
        var newFrame = new ExecutionFrame();
        newFrame.childIndentLevel = currentChildIndentLevel;
        newFrame.enterIndex = i;
        newFrame.startIndex = i + 1;
        newFrame.revolutionsCount = result.loopCount();
        executionFrames.add(newFrame);
    }

    public static CustomCommentValidationResult isValidCustomCommand(String line) {
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

    public static boolean isValidMinecraftCommand(
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
}
