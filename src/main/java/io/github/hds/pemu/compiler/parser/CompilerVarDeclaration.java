package io.github.hds.pemu.compiler.parser;

import io.github.hds.pemu.tokenizer.Token;
import org.jetbrains.annotations.NotNull;

public final class CompilerVarDeclaration {
    private final @NotNull String NAME;
    private final @NotNull Token TOKEN;

    protected CompilerVarDeclaration(@NotNull String name, @NotNull Token token) {
        NAME = name;
        TOKEN = token;
    }

    public @NotNull String getName() {
        return NAME;
    }

    public @NotNull Token getToken() {
        return TOKEN;
    }
}
