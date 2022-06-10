package io.github.marco4413.pemu.memory.flags;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Basic Flag that holds its value inside its instance
 */
public class BasicFlag extends AbstractFlag {

    private boolean value;

    public BasicFlag(boolean value, @NotNull String fullName) {
        this(value, fullName, StringUtils.toShortName(fullName));
    }

    public BasicFlag(boolean value, @Nullable String fullName, @NotNull String shortName) {
        super(fullName, shortName);
        this.value = value;
    }

    @Override
    public synchronized boolean getValue() {
        return this.value;
    }

    @Override
    public synchronized boolean setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
