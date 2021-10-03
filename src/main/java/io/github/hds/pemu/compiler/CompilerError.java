package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CompilerError extends RuntimeException {
    public static class ReferenceError extends CompilerError {
        protected ReferenceError(@Nullable File file, int errorLine, int errorLineChar, @NotNull String type, @NotNull String name, @NotNull String description) {
            super(
                    file, errorLine, errorLineChar, "Reference",
                    String.format("%s '%s' %s", type, name, description)
            );
        }
    }

    protected CompilerError(@Nullable File file, @NotNull String errorName, @NotNull String message) {
        this(file, -1, -1, errorName, message);
    }

    protected CompilerError(@Nullable File file, int errorLine, int errorLineChar, @NotNull String errorName, @NotNull String message) {
        super(
                String.format(
                        "'%s': %s Error (%d:%d): %s",
                        file == null ? "Unknown" : file.getName(), errorName, errorLine, errorLineChar,
                        message.endsWith(".") ? message : (message + ".")
                )
        );
    }
}
