package io.github.hds.pemu.memory.flags;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A Basic Flag that holds its value inside its instance
 */
public class BasicFlag extends AbstractFlag {

    private boolean value;

    public BasicFlag(boolean value, @NotNull String fullName) {
        this(value, fullName, StringUtils.toShortName(fullName));
    }

    public BasicFlag(boolean value, @NotNull String fullName, @NotNull String shortName) {
        super(fullName, shortName);
        this.value = value;
    }

    @Override
    public boolean getValue() {
        return this.value;
    }

    @Override
    public boolean setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
