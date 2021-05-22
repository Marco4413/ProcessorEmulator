package io.github.hds.pemu.memory.flags;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An Abstract implementation of IFlag that adds the naming capabilities
 * that all Flags should have
 */
public abstract class AbstractFlag implements IFlag {

    private final String FULL_NAME;
    private final String SHORT_NAME;

    public AbstractFlag(@NotNull String fullName) {
        this(fullName, StringUtils.toShortName(fullName));
    }

    public AbstractFlag(@NotNull String fullName, @NotNull String shortName) {
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
