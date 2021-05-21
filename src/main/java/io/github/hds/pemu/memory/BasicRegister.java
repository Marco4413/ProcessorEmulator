package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasicRegister implements IRegister {

    private final String FULL_NAME;
    private final String SHORT_NAME;

    public BasicRegister(@NotNull String fullName) {
        this(fullName, StringUtils.toShortName(fullName));
    }

    public BasicRegister(@NotNull String fullName, @NotNull String shortName) {
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
    public abstract int getValue();

    @Override
    public abstract int setValue(int value);

}
