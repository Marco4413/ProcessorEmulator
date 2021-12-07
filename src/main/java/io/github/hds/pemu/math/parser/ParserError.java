package io.github.hds.pemu.math.parser;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ParserError extends RuntimeException {

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
        protected ReferenceError(@Nullable File file, int line, int lineChar, @NotNull String type, @NotNull String name, @NotNull String description) {
            super(
                    file, line, lineChar, "Reference",
                    String.format("%s '%s' %s", type, name, description)
            );
        }
    }

    public static class OperatorError extends ParserError {
        protected OperatorError(@Nullable File file, int line, int lineChar, @NotNull String message) {
            super(file, line, lineChar, "Operator", message);
        }
    }

    public static class VariableError extends ParserError {
        protected VariableError(@Nullable File file, int line, int lineChar, @NotNull String message) {
            super(file, line, lineChar, "Variable", message);
        }
    }

    // Actual Error Implementation

    protected ParserError(@NotNull ParserContext ctx, @NotNull String errorName, @NotNull String message) {
        this(ctx.SOURCE_FILE, ctx.SOURCE_LINE + ctx.getCurrentLine(), ctx.getCurrentLineChar(), errorName, message);
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
