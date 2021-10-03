package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public final class StringNode implements INode {
    private final String STRING;

    protected StringNode(@NotNull String str) {
        STRING = str;
    }

    public @NotNull String getString() {
        return STRING;
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.STRING;
    }
}
