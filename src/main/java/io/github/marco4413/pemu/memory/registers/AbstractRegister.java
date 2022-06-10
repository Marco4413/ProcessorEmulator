package io.github.marco4413.pemu.memory.registers;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Abstract implementation of IRegister that adds the naming capabilities
 * that all Registers should have
 */
public abstract class AbstractRegister implements IRegister {

    private final String FULL_NAME;
    private final String SHORT_NAME;

    public AbstractRegister(@NotNull String fullName) {
        this(fullName, StringUtils.toShortName(fullName));
    }

    public AbstractRegister(@Nullable String fullName, @NotNull String shortName) {
        FULL_NAME = fullName;
        SHORT_NAME = shortName;
    }

    @Override
    public @Nullable String getFullName() {
        return FULL_NAME;
    }

    @Override
    public @NotNull String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public abstract int getValue();

    @Override
    public abstract int setValue(int value);

}
