package io.github.marco4413.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class AbstractTokenDefinition implements ITokenDefinition {

    private final String NAME;

    public AbstractTokenDefinition(@NotNull String name) {
        NAME = name;
    }

    @Override
    public abstract @NotNull Pattern getPattern();

    @Override
    public @NotNull String getName() {
        return NAME;
    }

}
