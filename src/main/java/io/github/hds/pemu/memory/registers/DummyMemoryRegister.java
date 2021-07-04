package io.github.hds.pemu.memory.registers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// We don't care about giving it a null Memory, because it's not used
@SuppressWarnings("all")
public final class DummyMemoryRegister extends MemoryRegister {
    public DummyMemoryRegister(@NotNull String fullName) {
        super(0, fullName, null, -1);
    }

    public DummyMemoryRegister(@Nullable String fullName, @NotNull String shortName) {
        super(0, fullName, shortName, null, -1);
    }

    @Override
    public synchronized int getValue() {
        return 0;
    }

    @Override
    public synchronized int setValue(int value) {
        return 0;
    }
}
