package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.parser.INode;
import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CompiledProgram {
    public static final long NO_COMPILE_TIME = -1;

    private final @NotNull IProcessor PROCESSOR;
    private final INode[] NODES;
    private final int[] PROGRAM;
    private final long COMPILE_TIME;

    protected CompiledProgram(@NotNull IProcessor processor, @NotNull List<INode> nodes, int[] program, long compileTime) {
        PROCESSOR = processor;
        NODES = nodes.toArray(new INode[0]);
        PROGRAM = program;
        COMPILE_TIME = compileTime < 0 ? NO_COMPILE_TIME : compileTime;
    }

    public @NotNull IProcessor getProcessor() {
        return PROCESSOR;
    }

    public @NotNull INode[] getNodes() {
        return NODES;
    }

    public int[] getProgram() {
        return PROGRAM;
    }

    public boolean hasCompileTime() {
        return COMPILE_TIME != NO_COMPILE_TIME;
    }

    public double getCompileTime() {
        return COMPILE_TIME / 1_000_000_000d;
    }
}
