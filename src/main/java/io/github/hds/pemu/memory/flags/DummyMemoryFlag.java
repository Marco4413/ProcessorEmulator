package io.github.hds.pemu.memory.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DummyMemoryFlag extends AbstractFlag implements IDummyFlag, IMemoryFlag {
    public DummyMemoryFlag(@NotNull String fullName) {
        super(fullName);
    }

    public DummyMemoryFlag(@Nullable String fullName, @NotNull String shortName) {
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

    @Override
    public int getAddress() {
        return 0;
    }

    @Override
    public int getBit() {
        return 0;
    }
}
