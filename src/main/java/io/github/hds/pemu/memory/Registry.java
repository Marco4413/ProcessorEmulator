package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Registry {

    public final String NAME;
    public final String SHORT;

    public int value = 0;

    public Registry(@NotNull String name) {
        this(name, StringUtils.toShortName(name));
    }

    public Registry(@NotNull String name, @NotNull String shortName) {
        NAME = name;
        SHORT = shortName;
    }

    public int getValue() {
        return value;
    }

    public int setValue(int value) {
        int oldValue = this.value;
        this.value = value;
        return oldValue;
    }

}
