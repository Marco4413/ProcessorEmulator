package io.github.hds.pemu.compiler.parser;

import io.github.hds.pemu.utils.IPIntSupplier;
import org.jetbrains.annotations.NotNull;

public final class ArrayNode implements INode {
    private final IPIntSupplier LENGTH;

    protected ArrayNode(@NotNull IPIntSupplier length) {
        LENGTH = length;
    }

    public int getLength() {
        return LENGTH.get();
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.ARRAY;
    }
}
