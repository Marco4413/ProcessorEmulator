package io.github.hds.pemu.compiler;

import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

public class CompiledProgram {
    private final @NotNull IProcessor PROCESSOR;
    private final @NotNull LabelData LABELS;
    private final @NotNull RegisterData REGISTERS;
    private final @NotNull OffsetsData OFFSETS;
    private final int[] PROGRAM;

    protected CompiledProgram(@NotNull IProcessor processor, @NotNull LabelData labels, @NotNull RegisterData registers, @NotNull OffsetsData offsets, int[] program) {
        PROCESSOR = processor;
        LABELS = labels;
        REGISTERS = registers;
        OFFSETS = offsets;
        PROGRAM = program;
    }

    public @NotNull IProcessor getProcessor() {
        return PROCESSOR;
    }

    public @NotNull LabelData getLabels() {
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
}
