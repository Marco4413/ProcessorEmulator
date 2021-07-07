package io.github.hds.pemu.memory.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DummyFlag extends AbstractFlag implements IDummyFlag {

    public DummyFlag(@NotNull String fullName) {
        super(fullName);
    }

    public DummyFlag(@Nullable String fullName, @NotNull String shortName) {
        super(fullName, shortName);
    }

    @Override
    public boolean getValue() {
        return false;
    }

    @Override
    public boolean setValue(boolean value) {
        return false;
    }

}
