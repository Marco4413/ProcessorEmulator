package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import org.jetbrains.annotations.NotNull;

public class ProcessorConfig {
    public int bits;
    public int memSize;
    public @NotNull InstructionSet instructionSet;

    public ProcessorConfig() {
        this(16);
    }

    public ProcessorConfig(int bits) {
        this(bits, 256);
    }

    public ProcessorConfig(int bits, int memSize) {
        this(bits, memSize, BasicInstructions.BASIC_SET);
    }

    public ProcessorConfig(int bits, int memSize, @NotNull InstructionSet instructionSet) {
        switch (bits) {
            case Word.SizeBit8:
            case Word.SizeBit16:
            case Word.SizeBit24:
                this.bits = bits;
                break;
            default:
                this.bits = 16;
        }
        this.memSize = memSize;
        this.instructionSet = instructionSet;
    }
}
