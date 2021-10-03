package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public class ValueNode implements INode {

    private final IValueProvider VALUE_PROVIDER;

    protected ValueNode(int value) {
        this(() -> value);
    }

    protected ValueNode(@NotNull IValueProvider valueProvider) {
        VALUE_PROVIDER = valueProvider;
    }

    public int getValue() {
        return VALUE_PROVIDER.getValue();
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.VALUE;
    }
}
