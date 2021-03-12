package io.github.hds.pemu.instructions;

import io.github.hds.pemu.Processor;
import io.github.hds.pemu.memory.Memory;
import org.jetbrains.annotations.NotNull;

public class InstructionSet {

    private final Instruction[] INSTRUCTIONS;

    public InstructionSet(@NotNull Instruction[] instructions) {
        INSTRUCTIONS = instructions;
    }

    public int getKeyCode(String keyword) {
        for (int i = 0; i < INSTRUCTIONS.length; i++) {
            if (INSTRUCTIONS[i].KEYWORD.equals(keyword)) return i;
        }
        return -1;
    }

    public Instruction parse(@NotNull Memory memory, int address) {
        memory.validateAddress(address);

        int keycode = memory.getValueAt(address);
        if (keycode < 0 || keycode >= INSTRUCTIONS.length)
            throw new IllegalArgumentException("Unknown Instruction!");

        return INSTRUCTIONS[keycode];
    }

    public int parseAndExecute(@NotNull Processor processor, @NotNull Memory memory, int address) {
        Instruction instruction = parse(memory, address);
        if (!instruction.execute(processor, memory.getValuesAt(address + 1, instruction.LENGTH - 1)))
            return instruction.LENGTH;
        return 0;
    }

}
