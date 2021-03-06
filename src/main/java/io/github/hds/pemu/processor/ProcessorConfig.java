package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Word;
import io.github.hds.pemu.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

public final class ProcessorConfig {

    // Getting Max, Min and Default values for Processor's word size
    public static final int MAX_BITS = Word.WordBit24.TOTAL_BITS;
    public static final int MIN_BITS = Word.WordBit8.TOTAL_BITS;
    public static final int DEFAULT_BITS = Word.WordBit16.TOTAL_BITS;

    // The max memory size is the biggest number that can be represented with the biggest word size
    public static final int MAX_MEMORY_SIZE = Word.WordBit24.BIT_MASK;
    public static final int MIN_MEMORY_SIZE = Byte.SIZE;
    public static final int DEFAULT_MEMORY_SIZE = (int) Math.pow(2, 8);

    // We take these from Clock's static fields
    public static final int MAX_FREQUENCY = Clock.MAX_FREQUENCY;
    public static final int MIN_FREQUENCY = Clock.MIN_FREQUENCY;
    public static final int DEFAULT_FREQUENCY = 1000;

    private int bits;
    private int memorySize;
    private int clockFrequency;
    private @NotNull InstructionSet instructionSet;

    public ProcessorConfig(@NotNull InstructionSet instructionSet) {
        this(instructionSet, DEFAULT_BITS);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits) {
        this(instructionSet, bits, DEFAULT_MEMORY_SIZE);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits, int memorySize) {
        this(instructionSet, bits, memorySize, DEFAULT_FREQUENCY);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits, int memorySize, int clockFrequency) {
        this.instructionSet = instructionSet;
        setBits(bits);
        setMemorySize(memorySize);
        setClockFrequency(clockFrequency);
    }

    public ProcessorConfig(@NotNull ProcessorConfig config) {
        this(config.instructionSet, config.bits, config.memorySize, config.clockFrequency);
    }

    public @NotNull ProcessorConfig setBits(int bits) {
        this.bits = Word.getClosestSize(bits);
        return this;
    }

    public @NotNull ProcessorConfig setMemorySize(int memorySize) {
        this.memorySize = MathUtils.makeMultipleOf(Byte.SIZE, MathUtils.constrain(memorySize, MIN_MEMORY_SIZE, MAX_MEMORY_SIZE));
        return this;
    }

    public @NotNull ProcessorConfig setClockFrequency(int frequency) {
        this.clockFrequency = MathUtils.constrain(frequency, MIN_FREQUENCY, MAX_FREQUENCY);
        return this;
    }

    public @NotNull ProcessorConfig setInstructionSet(@NotNull InstructionSet instructionSet) {
        this.instructionSet = instructionSet;
        return this;
    }

    public int getBits() {
        return bits;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getClockFrequency() {
        return clockFrequency;
    }

    public @NotNull InstructionSet getInstructionSet() {
        return instructionSet;
    }
}
