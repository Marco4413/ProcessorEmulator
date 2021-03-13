package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Flag {

    public final String NAME;
    public final String SHORT;

    public boolean value;

    public Flag(boolean value, @NotNull String name) {
        this(value, name, StringUtils.toShortName(name));
    }

    public Flag(boolean value, @NotNull String name, @NotNull String shortName) {
        this.value = value;
        NAME = name;
        SHORT = shortName;
    }

    public boolean getValue() {
        return value;
    }

    public boolean setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;
        return oldValue;
    }

}
