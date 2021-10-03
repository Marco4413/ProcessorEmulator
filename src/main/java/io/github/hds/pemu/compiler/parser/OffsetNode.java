package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public final class OffsetNode extends ValueNode {
    protected OffsetNode(int value) {
        super(value);
    }

    protected OffsetNode(@NotNull IValueProvider valueProvider) {
        super(valueProvider);
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.OFFSET;
    }
}
