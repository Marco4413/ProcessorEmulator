package io.github.marco4413.pemu.compiler.parser;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ParserError extends RuntimeException {

    public static class FileError extends ParserError {
        protected FileError(@NotNull ParserContext ctx, @NotNull String message) {
            super(ctx, "File", message);
        }

        protected FileError(@NotNull File file, @NotNull String message) {
            super(file, "File", message);
        }
    }

    public static class SyntaxError extends ParserError {
        protected SyntaxError(@NotNull ParserContext ctx, @NotNull String expected, @Nullable String got) {
            this(ctx, expected, got, false);
        }

        protected SyntaxError(@NotNull ParserContext ctx, @NotNull String expected, @Nullable String got, boolean noCharEscape) {
            super(
                    ctx, "Syntax",
                    String.format(
                            "Expected %s, got '%s'",
                            expected, noCharEscape ? got : StringUtils.SpecialCharacters.escapeAll(String.valueOf(got))
                    )
            );
        }
    }

    public static class ReferenceError extends ParserError {
        protected ReferenceError(@NotNull ParserContext ctx, @NotNull String type, @NotNull String name, @NotNull String description) {
            super(
                    ctx, "Reference",
                    String.format("%s '%s' %s", type, name, description)
            );
        }
    }

    public static class TypeError extends ParserError {
        protected TypeError(@NotNull ParserContext ctx, @NotNull String message) {
            super(ctx, "Type", message);
        }
    }

    public static class ProcessorError extends ParserError {
        protected ProcessorError(@NotNull ParserContext ctx, @NotNull String message) {
            super(ctx, "Processor", message);
        }
    }

    public static class ArgumentsError extends ParserError {
        protected ArgumentsError(@NotNull ParserContext ctx, @NotNull String instructionName, int argument, @NotNull String expected, @NotNull String got) {
            super(
                    ctx, "Arguments",
                    String.format("Invalid Argument %d to '%s' Instruction, expected %s, got '%s'", argument, instructionName, expected, got)
            );
        }
    }

    // Actual Error Implementation

    protected ParserError(@NotNull ParserContext ctx, @NotNull String errorName, @NotNull String message) {
        this(ctx.getCurrentFile(), ctx.getCurrentLine(), ctx.getCurrentLineChar(), errorName, message);
    }

    protected ParserError(@Nullable File file, @NotNull String errorName, @NotNull String message) {
        this(file, -1, -1, errorName, message);
    }

    protected ParserError(@Nullable File file, int errorLine, int errorLineChar, @NotNull String errorName, @NotNull String message) {
        super(
                String.format(
                        "'%s': %s Error (%d:%d): %s",
                        file == null ? "Unknown" : file.getName(), errorName, errorLine, errorLineChar,
                        message.endsWith(".") ? message : (message + ".")
                )
        );
    }
}
