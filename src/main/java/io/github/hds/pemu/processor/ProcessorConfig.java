package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.Instructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Word;
import io.github.hds.pemu.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

public class ProcessorConfig {

    public static final int MAX_BITS = Word.SizeBit24;
    public static final int MIN_BITS = Word.SizeBit8;
    public static final int DEFAULT_BITS = Word.SizeBit16;

    public static final int MAX_MEMORY_SIZE = Word.MaskBit24;
    public static final int MIN_MEMORY_SIZE = Byte.SIZE;
    public static final int DEFAULT_MEMORY_SIZE = (int) Math.pow(2, 8);

    public static final int MAX_CLOCK = Clock.MAX_CLOCK;
    public static final int MIN_CLOCK = Clock.MIN_CLOCK;
    public static final int DEFAULT_CLOCK = 1000;

    private int bits;
    private int memSize;
    private int clock;
    public @NotNull InstructionSet instructionSet;

    public ProcessorConfig() {
        this(DEFAULT_BITS);
    }

    public ProcessorConfig(int bits) {
        this(bits, DEFAULT_MEMORY_SIZE);
    }

    public ProcessorConfig(int bits, int memSize) {
        this(bits, memSize, DEFAULT_CLOCK);
    }

    public ProcessorConfig(int bits, int memSize, int clock) {
        this(bits, memSize, clock, Instructions.SET);
    }

    public ProcessorConfig(int bits, int memSize, int clock, @NotNull InstructionSet instructionSet) {
        setBits(bits);
        setMemorySize(memSize);
        setClock(clock);
        this.instructionSet = instructionSet;
    }

    public ProcessorConfig(@NotNull ProcessorConfig config) {
        this(config.bits, config.memSize, config.clock, config.instructionSet);
    }

    public void setBits(int bits) {
        this.bits = Word.getClosestSize(bits);
    }

    public void setMemorySize(int memSize) {
        this.memSize = MathUtils.makeMultipleOf(Byte.SIZE, MathUtils.constrain(memSize, MIN_MEMORY_SIZE, MAX_MEMORY_SIZE));
    }

    public void setClock(int clock) {
        this.clock = MathUtils.constrain(clock, Clock.MIN_CLOCK, Clock.MAX_CLOCK);
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
}
