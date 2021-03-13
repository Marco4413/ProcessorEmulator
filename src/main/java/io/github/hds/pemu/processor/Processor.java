package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.BasicInstructions;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import org.jetbrains.annotations.NotNull;

public class Processor {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Flag ZERO  = new Flag(false, "Zero Bit");
    public final Flag CARRY = new Flag(false, "Carry Bit");

    public final Memory PROGRAM;
    public final Memory DATA;

    public final InstructionSet INSTRUCTIONSET;

    public Processor(int bits) {
        this(256, 256);
    }

    public Processor(int bits, int memSize) {
        this(bits, memSize, memSize);
    }

    public Processor(int bits, int dataSize, int programSize) {
        this(bits, dataSize, programSize, BasicInstructions.BASIC_SET);
    }

    public Processor(int bits, int dataSize, int programSize, InstructionSet instructionSet) {
        Word word = new Word(bits);
        PROGRAM = new Memory(programSize, word);
        DATA = new Memory(dataSize, word);

        SP.value = DATA.getSize() - 1;

        INSTRUCTIONSET = instructionSet;
    }

    public @NotNull String getInfo() {
        return "\tData Memory:\t" + DATA.getSize() + 'x' + DATA.WORD.BYTES + " Bytes\n" +
               "\tProgram Memory:\t" + PROGRAM.getSize() + 'x' + PROGRAM.WORD.BYTES + " Bytes\n" +
               "\tInstructions:\t" + INSTRUCTIONSET.getSize() + "\n";
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
