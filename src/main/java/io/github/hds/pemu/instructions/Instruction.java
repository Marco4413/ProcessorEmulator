package io.github.hds.pemu.instructions;

import io.github.hds.pemu.Processor;
import org.jetbrains.annotations.NotNull;

public class Instruction {

    public final String KEYWORD;
    public final int LENGTH;

    public Instruction(@NotNull String keyword, int length) {
        KEYWORD = keyword;
        LENGTH = length + 1;
    }

    public boolean execute(@NotNull Processor p, int[] args) { return false; }

}
