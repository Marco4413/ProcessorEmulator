package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.Instructions;
import io.github.hds.pemu.instructions.InstructionSet;
import org.jetbrains.annotations.NotNull;

public class ProcessorConfig {
    public int bits;
    public int memSize;
    public int clock;
    public @NotNull InstructionSet instructionSet;

    public ProcessorConfig() {
        this(16);
    }

    public ProcessorConfig(int bits) {
        this(bits, 256);
    }

    public ProcessorConfig(int bits, int memSize) {
        this(bits, memSize, 1000);
    }

    public ProcessorConfig(int bits, int memSize, int clock) {
        this(bits, memSize, clock, Instructions.SET);
    }

    public ProcessorConfig(int bits, int memSize, int clock, @NotNull InstructionSet instructionSet) {
        this.bits = bits;
        this.memSize = memSize;
        this.clock = clock;
        this.instructionSet = instructionSet;
    }

    public ProcessorConfig(@NotNull ProcessorConfig config) {
        this(config.bits, config.memSize, config.clock, config.instructionSet);
    }
}
