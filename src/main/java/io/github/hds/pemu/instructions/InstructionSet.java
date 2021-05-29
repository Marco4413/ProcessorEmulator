package io.github.hds.pemu.instructions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InstructionSet {

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

}
