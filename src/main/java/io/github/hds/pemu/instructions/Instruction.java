package io.github.hds.pemu.instructions;

import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

public class Instruction {

    private final String KEYWORD;
    private final int ARGUMENTS;

    public Instruction(@NotNull String keyword, int arguments) {
        KEYWORD = keyword;
        ARGUMENTS = arguments;
    }

    public int getWords() {
        return ARGUMENTS + 1;
    }

    public @NotNull String getKeyword() {
        return KEYWORD;
    }

    public int getArgumentsCount() {
        return ARGUMENTS;
    }

    public void execute(@NotNull IProcessor p, int[] args) { }

}
