package io.github.marco4413.pemu.memory.registers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DummyMemoryRegister extends AbstractRegister implements IDummyRegister, IMemoryRegister {
    public DummyMemoryRegister(@NotNull String fullName) {
        super(fullName);
    }

    public DummyMemoryRegister(@Nullable String fullName, @NotNull String shortName) {
        super(fullName, shortName);
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public int setValue(int value) {
        return 0;
    }

    @Override
    public int getAddress() {
        return 0;
    }
}
