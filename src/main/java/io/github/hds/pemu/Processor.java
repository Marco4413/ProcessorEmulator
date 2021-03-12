package io.github.hds.pemu;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;

public class Processor {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Memory MEMORY;

    public final InstructionSet INSTRUCTIONSET;

    public Processor() {
        this(256, BasicInstructions.BASIC_SET);
    }

    public Processor(int memSize) {
        this(memSize, BasicInstructions.BASIC_SET);
    }

    public Processor(InstructionSet instructionSet) {
        this(256, instructionSet);
    }

    public Processor(int memSize, InstructionSet instructionSet) {
        MEMORY = new Memory(memSize);
        SP.setValue(MEMORY.getSize() - 1);

        INSTRUCTIONSET = instructionSet;
    }

    public void run() {
        if (isRunning) return;

        isRunning = true;
        while (isRunning) {

            INSTRUCTIONSET.parseAndExecute(this, IP.getValue());

        }
    }

    public void run(int fromAddress) {
        if (isRunning) return;

        IP.setValue(fromAddress);
        run();
    }

    public void stop() { isRunning = false; }

}
