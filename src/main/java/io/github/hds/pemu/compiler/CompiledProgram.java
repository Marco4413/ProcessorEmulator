package io.github.hds.pemu.compiler;

import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CompiledProgram {
    private final @NotNull IProcessor PROCESSOR;
    private final @NotNull LabelData LABELS;
    private final @NotNull RegisterData REGISTERS;
    private final int[] DATA;

    protected CompiledProgram(@NotNull IProcessor processor, @NotNull LabelData labels, @NotNull RegisterData registers, int[] programData) {
        PROCESSOR = processor;
        LABELS = labels;
        REGISTERS = registers;
        DATA = programData;
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

    public int[] getData() {
        return DATA;
    }
}
