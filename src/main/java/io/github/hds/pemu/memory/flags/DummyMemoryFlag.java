package io.github.hds.pemu.memory.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// We don't care about giving it a null Memory, because it's not used
@SuppressWarnings("all")
public final class DummyMemoryFlag extends MemoryFlag {
    public DummyMemoryFlag(@NotNull String fullName) {
        super(false, fullName, null, -1, -1);
    }

    public DummyMemoryFlag(@Nullable String fullName, @NotNull String shortName) {
        super(false, fullName, shortName, null, -1, -1);
    }

    @Override
    public synchronized boolean getValue() {
        return false;
    }

    @Override
    public synchronized boolean setValue(boolean value) {
        return false;
    }
}
