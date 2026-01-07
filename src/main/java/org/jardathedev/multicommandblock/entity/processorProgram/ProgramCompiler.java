package org.jardathedev.multicommandblock.entity.processorProgram;

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

import java.util.List;

import static org.jardathedev.multicommandblock.util.CommandUtil.*;
import static org.jardathedev.multicommandblock.util.CommandUtil.CUSTOM_COMMAND_START_CHAR;
import static org.jardathedev.multicommandblock.util.CommandUtil.MINECRAFT_COMMAND_START_CHAR;
import static org.jardathedev.multicommandblock.util.CommandUtil.endCorrespondingFrames;

public class ProgramCompiler {
    public static boolean compileLines(List<String> linesToCompile, List<CommandLine> programLines, List<ExecutionFrame> executionFrames, BlockEntityAttributes attrs, CmdSourceStore cmdSourceStore) {
        programLines.clear();
        executionFrames.clear();
        MinecraftServer server = attrs.world().getServer();

        boolean hasErrors = false;
        int currentChildIndentLevel = 0;
        CommandLine programLine = null;
        int lastExecutableLineIndex = 0;

        for (int i = 0; i <= linesToCompile.size(); i++) {
            if (programLine != null) {
                programLines.add(programLine);
            }
            if (i == linesToCompile.size())
                break;

            String line = linesToCompile.get(i);
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
                if (isValidMinecraftCommand(trimmed.substring(1), cmdSourceStore.getSource(attrs), server.getCommandManager())) {
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
                        if (i + 1 < linesToCompile.size()) {
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
        return !hasErrors;
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
