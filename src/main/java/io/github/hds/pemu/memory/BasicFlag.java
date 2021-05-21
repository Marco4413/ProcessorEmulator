package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasicFlag implements IFlag {

    private final String FULL_NAME;
    private final String SHORT_NAME;

    public BasicFlag(@NotNull String fullName) {
        this(fullName, StringUtils.toShortName(fullName));
    }

    public BasicFlag(@NotNull String fullName, @NotNull String shortName) {
        FULL_NAME = fullName;
        SHORT_NAME = shortName;
    }

    @Override
    public @Nullable String getFullName() {
        return FULL_NAME;
    }

    @Override
    public @NotNull String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public abstract boolean getValue();

    @Override
    public abstract boolean setValue(boolean value);

}
