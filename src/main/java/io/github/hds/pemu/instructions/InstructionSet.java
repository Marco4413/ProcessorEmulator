package io.github.hds.pemu.instructions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

public final class InstructionSet {

    private Instruction[] INSTRUCTIONS;

    public InstructionSet(@NotNull Instruction[] instructions) {
        INSTRUCTIONS = Arrays.copyOf(instructions, instructions.length);
    }

    public InstructionSet(int[] opcodes, @NotNull Instruction[] instructions) {
        IntSummaryStatistics opcodesStats = Arrays.stream(opcodes).summaryStatistics();
        this._init(opcodesStats.getMax(), opcodes, instructions);
    }

    public InstructionSet(int maxOpcode, int[] opcodes, @NotNull Instruction[] instructions) {
        this._init(maxOpcode, opcodes, instructions);
    }

    private void _init(int maxOpcode, int[] opcodes, @NotNull Instruction[] instructions) {
        if (maxOpcode < 0)
            throw new IllegalArgumentException("Got negative Max Opcode: " + maxOpcode);
        if (opcodes.length != instructions.length)
            throw new IllegalArgumentException("Opcodes and Instructions must be the same length");

        INSTRUCTIONS = new Instruction[maxOpcode + 1];
        for (int i = 0; i < opcodes.length; i++) {
            int opcode = opcodes[i];
            if (opcode < 0)
                throw new IllegalArgumentException("Got negative Opcode: " + opcode);
            else if (opcode > maxOpcode)
                throw new IllegalArgumentException("Opcode '" + opcode + "' Is above the max one: " + maxOpcode);

            INSTRUCTIONS[opcode] = instructions[i];
        }
    }

    public int getOpcode(@NotNull String keyword) {
        for (int i = 0; i < INSTRUCTIONS.length; i++) {
            Instruction instruction = INSTRUCTIONS[i];
            if (instruction != null && instruction.getKeyword().equals(keyword)) return i;
        }
        return -1;
    }

    public @Nullable Instruction getInstruction(int opcode) {
        if (opcode < 0 || opcode >= INSTRUCTIONS.length) return null;
        return INSTRUCTIONS[opcode];
    }

    public @Nullable Instruction getInstruction(@NotNull String keyword) {
        int keycode = getOpcode(keyword);
        if (keycode < 0) return null;
        return INSTRUCTIONS[keycode];
    }

    public int getSize() {
        return INSTRUCTIONS.length;
    }

}
