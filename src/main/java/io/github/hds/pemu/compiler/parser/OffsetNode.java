package io.github.hds.pemu.compiler.parser;

import io.github.hds.pemu.utils.IPIntSupplier;
import org.jetbrains.annotations.NotNull;

public final class OffsetNode extends ValueNode {
    protected OffsetNode(int value) {
        super(value);
    }

    protected OffsetNode(@NotNull IPIntSupplier valueSupplier) {
        super(valueSupplier);
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.OFFSET;
    }
}
