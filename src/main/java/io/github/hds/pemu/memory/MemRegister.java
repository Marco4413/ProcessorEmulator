package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MemRegister extends BasicRegister {

    private final @NotNull Memory BOUND_MEMORY;
    private int boundAddress;

    public MemRegister(int value, @NotNull String fullName, @NotNull Memory boundMemory, int boundAddress) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress);
    }

    public MemRegister(int value, @NotNull String fullName, @NotNull String shortName, @NotNull Memory boundMemory, int boundAddress) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        this.boundAddress = boundAddress;

        setValue(value);
    }

    @Override
    public int getValue() {
        return BOUND_MEMORY.getValueAt(boundAddress);
    }

    @Override
    public int setValue(int value) {
        return BOUND_MEMORY.setValueAt(boundAddress, value);
    }
}
