package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class Processor implements Runnable {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Flag ZERO  = new Flag(false, "Zero Bit");
    public final Flag CARRY = new Flag(false, "Carry Bit");

    public final Memory MEMORY;
    public final Clock CLOCK;

    public final InstructionSet INSTRUCTIONSET;

    public volatile char pressedChar = '\0';
    public volatile int pressedKey = KeyEvent.VK_UNDEFINED;
    private long startTimestamp = 0;

    public Processor(int bits) {
        this(bits, 256);
    }

    public Processor(int bits, int memSize) {
        this(bits, memSize, 1000);
    }

    public Processor(int bits, int memSize, double clock) {
        this(bits, memSize, clock, BasicInstructions.BASIC_SET);
    }

    public Processor(int bits, int memSize, double clock, @NotNull InstructionSet instructionSet) {
        Word word = new Word(bits);
        MEMORY = new Memory(memSize, word);
        CLOCK = new Clock(clock);

        SP.value = MEMORY.getSize() - 1;

        INSTRUCTIONSET = instructionSet;
    }

    public Processor(@NotNull ProcessorConfig config) {
        this(config.bits, config.memSize, config.clock, config.instructionSet);
    }

    public void updateFlags(int value, boolean zero, boolean carry) {
        if (zero) ZERO.value = value == 0;
        if (carry) CARRY.value = (value & ~MEMORY.WORD.MASK) != 0;
    }

    public @NotNull String getInfo() {
        return "\tMemory:\t" + MEMORY.getSize() + 'x' + MEMORY.WORD.BYTES + " Bytes\n" +
               "\tInstructions:\t" + INSTRUCTIONSET.getSize() + "\n";
    }

    public long getTimeRunning() {
        if (!isRunning) return -1;
        return System.currentTimeMillis() - startTimestamp;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void run() {
        if (isRunning) return;

        startTimestamp = System.currentTimeMillis();
        isRunning = true;
        while (isRunning) {

            if (CLOCK.update()) {
                int instructionLength = INSTRUCTIONSET.parseAndExecute(this, MEMORY, IP.value);
                IP.value += instructionLength;

                if (IP.value >= MEMORY.getSize()) stop();
            }

        }
    }

    public void stop() { isRunning = false; }

}
