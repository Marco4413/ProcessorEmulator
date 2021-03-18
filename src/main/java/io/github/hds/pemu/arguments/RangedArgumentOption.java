package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RangedArgumentOption <T extends Comparable<T>> extends ArgumentOption<T> {

    public final T MAX_VALUE;
    public final T MIN_VALUE;

    public RangedArgumentOption(@NotNull String name, @Nullable String shortName, @NotNull T defaultValue, @NotNull T minValue, @NotNull T maxValue) {
        super(name, shortName, defaultValue);
        MAX_VALUE = maxValue;
        MIN_VALUE = minValue;
    }

    public void validate() {
        if (this.value.compareTo(MAX_VALUE) > 0) this.value = MAX_VALUE;
        else if (this.value.compareTo(MIN_VALUE) < 0) this.value = MIN_VALUE;
    }

    @Override
    public String valueToString() {
        return super.valueToString() + "[" + MIN_VALUE.toString() + "; " + MAX_VALUE.toString() + "]";
    }
}
