package io.github.hds.pemu.instructions;

import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

public class Instruction {

    public final String KEYWORD;
    public final int WORDS;

    public Instruction(@NotNull String keyword, int arguments) {
        KEYWORD = keyword;
        WORDS = arguments + 1;
    }

    public boolean execute(@NotNull Processor p, int[] args) { return false; }

}
