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

    protected void constrain() {
        T optValue = getValue();
        if (optValue.compareTo(MAX_VALUE) > 0) setValue(MAX_VALUE);
        else if (optValue.compareTo(MIN_VALUE) < 0) setValue(MIN_VALUE);
    }

    @Override
    public @NotNull String valueTypeToString() {
        return super.valueTypeToString() + "[" + MIN_VALUE.toString() + "; " + MAX_VALUE.toString() + "]";
    }
}
