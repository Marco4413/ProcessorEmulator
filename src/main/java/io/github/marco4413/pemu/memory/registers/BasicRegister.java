package io.github.marco4413.pemu.memory.registers;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Basic Register that holds its value inside its instance
 */
public class BasicRegister extends AbstractRegister {

    private int value;

    public BasicRegister(int value, @NotNull String fullName) {
        this(value, fullName, StringUtils.toShortName(fullName));
    }

    public BasicRegister(int value, @Nullable String fullName, @NotNull String shortName) {
        super(fullName, shortName);
        this.value = value;
    }

    @Override
    public synchronized int getValue() {
        return this.value;
    }

    @Override
    public synchronized int setValue(int value) {
        int oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
