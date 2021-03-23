package io.github.hds.pemu.instructions;

import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.memory.Memory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InstructionSet {

    public static class ExecutionData {
        public final Instruction INSTRUCTION;
        public final int LENGTH;
        public final boolean IGNORE_LENGTH;

        protected ExecutionData(@NotNull Instruction instruction) {
            this(instruction, 0);
        }

        protected ExecutionData(@NotNull Instruction instruction, int length) {
            this(instruction, length, false);
        }

        protected ExecutionData(@NotNull Instruction instruction, int length, boolean ignoreLength) {
            INSTRUCTION = instruction;
            LENGTH = length;
            IGNORE_LENGTH = ignoreLength;
        }
    }

    private final Instruction[] INSTRUCTIONS;

    public InstructionSet(@NotNull Instruction[] instructions) {
        INSTRUCTIONS = instructions;
    }

    public int getKeyCode(@NotNull String keyword) {
        for (int i = 0; i < INSTRUCTIONS.length; i++) {
            if (INSTRUCTIONS[i].KEYWORD.equals(keyword)) return i;
        }
        return -1;
    }

    public @Nullable Instruction getInstruction(int keycode) {
        if (keycode < 0 || keycode >= INSTRUCTIONS.length) return null;
        return INSTRUCTIONS[keycode];
    }

    public @Nullable Instruction getInstruction(@NotNull String keyword) {
        int keycode = getKeyCode(keyword);
        if (keycode < 0) return null;
        return INSTRUCTIONS[keycode];
    }

    public int getSize() {
        return INSTRUCTIONS.length;
    }

    public Instruction parse(@NotNull Memory memory, int address) {
        memory.validateAddress(address);

        int keycode = memory.getValueAt(address);
        if (keycode < 0 || keycode >= INSTRUCTIONS.length)
            throw new IllegalArgumentException("Unknown Instruction!");

        return INSTRUCTIONS[keycode];
    }

    public ExecutionData parseAndExecute(@NotNull Processor processor, @NotNull Memory memory, int address) {
        Instruction instruction = parse(memory, address);
        return new ExecutionData(
                instruction, instruction.getWords(),
                instruction.execute(processor, instruction.ARGUMENTS == 0 ? new int[0] : memory.getValuesAt(address + 1, instruction.ARGUMENTS))
        );
    }

}
