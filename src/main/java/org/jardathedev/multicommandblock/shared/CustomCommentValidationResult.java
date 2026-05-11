package org.jardathedev.multicommandblock.shared;

public record CustomCommentValidationResult(boolean isValid, boolean definesNewExecutionFrame, int loopCount) {
}
