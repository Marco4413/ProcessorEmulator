package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import org.jetbrains.annotations.NotNull;

public class Processor implements Runnable {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Flag ZERO  = new Flag(false, "Zero Bit");
    public final Flag CARRY = new Flag(false, "Carry Bit");

    public final Memory MEMORY;

    public final InstructionSet INSTRUCTIONSET;

    public Processor(int bits) {
        this(256, 256);
    }

    public Processor(int bits, int memSize) {
        this(bits, memSize, BasicInstructions.BASIC_SET);
    }

    public Processor(int bits, int memSize, @NotNull InstructionSet instructionSet) {
        Word word = new Word(bits);
        MEMORY = new Memory(memSize, word);

        SP.value = MEMORY.getSize() - 1;

        INSTRUCTIONSET = instructionSet;
    }

    public Processor(@NotNull ProcessorConfig config) {
        this(config.bits, config.memSize, config.instructionSet);
    }

    public @NotNull String getInfo() {
        return "\tMemory:\t" + MEMORY.getSize() + 'x' + MEMORY.WORD.BYTES + " Bytes\n" +
               "\tInstructions:\t" + INSTRUCTIONSET.getSize() + "\n";
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void run() {
        if (isRunning) return;

        isRunning = true;
        while (isRunning) {

            int instructionLength = INSTRUCTIONSET.parseAndExecute(this, MEMORY, IP.value);
            IP.value += instructionLength;

            if (IP.value >= MEMORY.getSize()) stop();

        }
    }

    public void stop() { isRunning = false; }

}
