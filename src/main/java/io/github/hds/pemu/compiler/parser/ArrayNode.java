package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public final class ArrayNode implements INode {
    private final IValueProvider LENGTH;

    protected ArrayNode(@NotNull IValueProvider length) {
        LENGTH = length;
    }

    public int getLength() {
        return LENGTH.getValue();
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.ARRAY;
    }
}
