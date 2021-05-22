package io.github.hds.pemu.memory.registers;

import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A Register that is held in Memory
 */
public class MemoryRegister extends AbstractRegister {

    private final @NotNull Memory BOUND_MEMORY;
    private final int BOUND_ADDRESS;

    public MemoryRegister(int value, @NotNull String fullName, @NotNull Memory boundMemory, int boundAddress) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress);
    }

    public MemoryRegister(int value, @NotNull String fullName, @NotNull String shortName, @NotNull Memory boundMemory, int boundAddress) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        BOUND_ADDRESS = boundAddress;

        setValue(value);
    }

    /**
     * Returns the Memory address where this Register is held
     * @return The address at which this Register is held in Memory
     */
    public int getAddress() {
        return BOUND_ADDRESS;
    }

    @Override
    public int getValue() {
        return BOUND_MEMORY.getValueAt(BOUND_ADDRESS);
    }

    @Override
    public int setValue(int value) {
        return BOUND_MEMORY.setValueAt(BOUND_ADDRESS, value);
    }
}
