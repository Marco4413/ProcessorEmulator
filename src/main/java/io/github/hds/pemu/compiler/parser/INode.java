package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

public interface INode {
    @NotNull NodeType getType();
}
