package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ArgumentOption <T> {
    public final @NotNull String NAME;
    public final @Nullable String SHORT;
    public @NotNull T value;
    public boolean specified = false;

    public ArgumentOption(@NotNull String name, @Nullable String shortName, @NotNull T defaultValue) {
        NAME = name;
        SHORT = shortName;
        value = defaultValue;
    }

    public boolean matches(@NotNull String command) {
        return command.equals(NAME) || command.equals(SHORT);
    }

    public abstract int getLength();
    public abstract void parse(@NotNull String[] args);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public String valueToString() {
        return value.getClass().getSimpleName();
    }
}
