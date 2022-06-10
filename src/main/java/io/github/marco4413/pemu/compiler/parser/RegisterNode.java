package io.github.marco4413.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public final class RegisterNode extends ValueNode {
    private final String NAME;

    protected RegisterNode(int address, @NotNull String name) {
        super(address);
        NAME = name;
    }

    public @NotNull String getName() {
        return NAME;
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.REGISTER;
    }
}
