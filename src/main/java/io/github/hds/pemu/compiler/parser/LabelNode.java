package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class LabelNode implements INode {

    private final @NotNull File FILE;
    private final int LINE;
    private final int LINE_CHAR;

    private final @NotNull String NAME;
    private final @NotNull IValueProvider OFFSET;
    private final boolean IS_DECLARATION;


    protected LabelNode(@NotNull ParserContext ctx, @NotNull String name, boolean isDeclaration) {
        this(ctx, name, 0, isDeclaration);
    }

    protected LabelNode(@NotNull ParserContext ctx, @NotNull String name, int offset, boolean isDeclaration) {
        this(ctx, name, () -> offset, isDeclaration);
    }

    protected LabelNode(@NotNull ParserContext ctx, @NotNull String name, @NotNull IValueProvider offset, boolean isDeclaration) {
        FILE = ctx.getCurrentFile();
        LINE = ctx.getCurrentLine();
        LINE_CHAR = ctx.getCurrentLineChar();

        NAME = name;
        OFFSET = offset;
        IS_DECLARATION = isDeclaration;
    }

    public @NotNull String getName() {
        return NAME;
    }

    public int getOffset() {
        return OFFSET.getValue();
    }

    public boolean isDeclaration() {
        return IS_DECLARATION;
    }

    public @NotNull File getFile() {
        return FILE;
    }

    public int getLine() {
        return LINE;
    }

    public int getLineChar() {
        return LINE_CHAR;
    }

    @Override
    public @NotNull NodeType getType() {
        return NodeType.LABEL;
    }
}
