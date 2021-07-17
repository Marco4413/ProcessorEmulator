package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.labels.OffsetLabel;
import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

public final class CompiledProgram {
    public static final long NO_COMPILE_TIME = -1;

    private final @NotNull IProcessor PROCESSOR;
    private final @NotNull LabelData<OffsetLabel> LABELS;
    private final @NotNull RegisterData REGISTERS;
    private final @NotNull OffsetsData OFFSETS;
    private final int[] PROGRAM;
    private final long COMPILE_TIME;

    protected CompiledProgram(@NotNull IProcessor processor, @NotNull LabelData<OffsetLabel> labels, @NotNull RegisterData registers, @NotNull OffsetsData offsets, int[] program, long compileTime) {
        PROCESSOR = processor;
        LABELS = labels;
        REGISTERS = registers;
        OFFSETS = offsets;
        PROGRAM = program;
        COMPILE_TIME = compileTime < 0 ? NO_COMPILE_TIME : compileTime;
    }

    public @NotNull IProcessor getProcessor() {
        return PROCESSOR;
    }

    public @NotNull LabelData<OffsetLabel> getLabels() {
        return LABELS;
    }

    public @NotNull RegisterData getRegisters() {
        return REGISTERS;
    }

    public @NotNull OffsetsData getOffsets() {
        return OFFSETS;
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
