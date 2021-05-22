package io.github.hds.pemu.memory.registers;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A Basic Register that holds its value inside its instance
 */
public class BasicRegister extends AbstractRegister {

    private int value;

    public BasicRegister(int value, @NotNull String fullName) {
        this(value, fullName, StringUtils.toShortName(fullName));
    }

    public BasicRegister(int value, @NotNull String fullName, @NotNull String shortName) {
        super(fullName, shortName);
        this.value = value;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public int setValue(int value) {
        int oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
