package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Token {

    private final String MATCH;
    private final String[] GROUPS;
    private final ITokenDefinition DEFINITION;

    private final int LINE;
    private final int LINE_CHAR;

    public Token(@NotNull String match, @NotNull ITokenDefinition definition, int line, int character) {
        this(match, new String[0], definition, line, character);
    }

    public Token(@NotNull String match, @NotNull String[] groups, @NotNull ITokenDefinition definition, int line, int lineChar) {
        this.MATCH = match;
        this.GROUPS = groups;
        this.DEFINITION = definition;
        this.LINE = line;
        this.LINE_CHAR = lineChar;
    }

    public @NotNull String getMatch() {
        return this.MATCH;
    }

    public @NotNull String[] getGroups() {
        return this.GROUPS;
    }

    public @NotNull ITokenDefinition getDefinition() {
        return this.DEFINITION;
    }

    public boolean isOfDefinition(@Nullable ITokenDefinition definition) {
        return DEFINITION.equals(definition);
    }

    public int getLine() {
        return this.LINE;
    }

    public int getLineChar() {
        return this.LINE_CHAR;
    }

}
