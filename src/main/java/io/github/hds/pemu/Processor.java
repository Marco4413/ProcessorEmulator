package io.github.hds.pemu;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;

public class Processor {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Memory PROGRAM;
    public final Memory DATA;

    public final InstructionSet INSTRUCTIONSET;

    public Processor() {
        this(Memory.MAX_UNSIGNED_BYTE);
    }

    public Processor(int memSize) {
        this(memSize, memSize);
    }

    public Processor(int dataSize, int programSize) {
        this(dataSize, programSize, BasicInstructions.BASIC_SET);
    }

    public Processor(int dataSize, int programSize, InstructionSet instructionSet) {
        PROGRAM = new Memory(programSize);
        DATA = new Memory(dataSize);

        SP.value = DATA.getSize() - 1;

        INSTRUCTIONSET = instructionSet;
    }

    public void run() {
        if (isRunning) return;

        isRunning = true;
        while (isRunning) {

            int instructionLength = INSTRUCTIONSET.parseAndExecute(this, PROGRAM, IP.value);
            IP.value += instructionLength;

        }
    }

    public void stop() { isRunning = false; }

}
