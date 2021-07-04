package io.github.hds.pemu.memory.registers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DummyRegister extends AbstractRegister {

    public DummyRegister(@NotNull String fullName) {
        super(fullName);
    }

    public DummyRegister(@Nullable String fullName, @NotNull String shortName) {
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

}
