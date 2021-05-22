package io.github.hds.pemu.compiler;

import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CompiledProgram {
    private final @NotNull IProcessor PROCESSOR;
    private final @NotNull HashMap<String, LabelData> LABELS;
    private final @NotNull HashMap<Integer, String> REGISTERS;
    private final int[] DATA;

    protected CompiledProgram(@NotNull IProcessor processor, @NotNull HashMap<String, LabelData> labels, @NotNull HashMap<Integer, String> registers, int[] programData) {
        PROCESSOR = processor;
        LABELS = labels;
        REGISTERS = registers;
        DATA = programData;
    }

    public @NotNull IProcessor getProcessor() {
        return PROCESSOR;
    }

    public @NotNull HashMap<String, LabelData> getLabels() {
        return LABELS;
    }

    public @NotNull HashMap<Integer, String> getRegisters() {
        return REGISTERS;
    }

    public int[] getData() {
        return DATA;
    }
}
