package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ArgumentOption <T> {
    private final @NotNull String NAME;
    private final @Nullable String SHORT;
    private @NotNull T value;
    private boolean specified = false;

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

    public @NotNull String getName() {
        return NAME;
    }

    public @Nullable String getShortName() {
        return SHORT;
    }

    protected void setValue(@NotNull T newValue) {
        value = newValue;
    }

    public @NotNull T getValue() {
        return value;
    }

    protected void setSpecified(boolean isIt) {
        specified = isIt;
    }

    public boolean isSpecified() {
        return specified;
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName();
    }

    public @NotNull String valueTypeToString() {
        return value.getClass().getSimpleName();
    }
}
