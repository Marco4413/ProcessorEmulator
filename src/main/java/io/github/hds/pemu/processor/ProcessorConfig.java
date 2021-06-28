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
    public static final int MAX_CLOCK = Clock.MAX_CLOCK;
    public static final int MIN_CLOCK = Clock.MIN_CLOCK;
    public static final int DEFAULT_CLOCK = 1000;

    private int bits;
    private int memSize;
    private int clock;
    private @NotNull InstructionSet instructionSet;

    public ProcessorConfig(@NotNull InstructionSet instructionSet) {
        this(instructionSet, DEFAULT_BITS);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits) {
        this(instructionSet, bits, DEFAULT_MEMORY_SIZE);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits, int memSize) {
        this(instructionSet, bits, memSize, DEFAULT_CLOCK);
    }

    public ProcessorConfig(@NotNull InstructionSet instructionSet, int bits, int memSize, int clock) {
        this.instructionSet = instructionSet;
        setBits(bits);
        setMemorySize(memSize);
        setClock(clock);
    }

    public ProcessorConfig(@NotNull ProcessorConfig config) {
        this(config.instructionSet, config.bits, config.memSize, config.clock);
    }

    public @NotNull ProcessorConfig setBits(int bits) {
        this.bits = Word.getClosestSize(bits);
        return this;
    }

    public @NotNull ProcessorConfig setMemorySize(int memSize) {
        this.memSize = MathUtils.makeMultipleOf(Byte.SIZE, MathUtils.constrain(memSize, MIN_MEMORY_SIZE, MAX_MEMORY_SIZE));
        return this;
    }

    public @NotNull ProcessorConfig setClock(int clock) {
        this.clock = MathUtils.constrain(clock, Clock.MIN_CLOCK, Clock.MAX_CLOCK);
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
        return memSize;
    }

    public int getClock() {
        return clock;
    }

    public @NotNull InstructionSet getInstructionSet() {
        return instructionSet;
    }
}
