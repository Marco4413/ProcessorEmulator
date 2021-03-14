package io.github.hds.pemu.instructions;

import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

public class Instruction {

    public final String KEYWORD;
    public final int ARGUMENTS;

    public Instruction(@NotNull String keyword, int arguments) {
        KEYWORD = keyword;
        ARGUMENTS = arguments;
    }

    public int getWords() {
        return ARGUMENTS + 1;
    }

    public boolean execute(@NotNull Processor p, int[] args) { return false; }

}
