package io.github.hds.pemu.memory.flags;

import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A Flag that is held in Memory
 */
public class MemoryFlag extends AbstractFlag {

    private final @NotNull Memory BOUND_MEMORY;
    private final int BOUND_ADDRESS;
    private final int BOUND_BIT;

    public MemoryFlag(boolean value, @NotNull String fullName, @NotNull Memory boundMemory, int boundAddress, int boundBit) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress, boundBit);
    }

    public MemoryFlag(boolean value, @NotNull String fullName, @NotNull String shortName, @NotNull Memory boundMemory, int boundAddress, int boundBit) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        BOUND_ADDRESS = boundAddress;
        BOUND_BIT = boundBit;

        setValue(value);
    }

    /**
     * Returns the Memory address where this Flag is held
     * @return The address at which this Flag is held in Memory
     */
    public int getAddress() {
        return BOUND_ADDRESS;
    }

    /**
     * Returns the Bit that holds this Flag's value
     * @return The Bit that holds this Flag's value
     */
    public int getBit() {
        return BOUND_BIT;
    }

    @Override
    public boolean getValue() {
        int boundValue = BOUND_MEMORY.getValueAt(BOUND_ADDRESS);
        return ( boundValue & (1 << BOUND_BIT) ) != 0;
    }

    @Override
    public boolean setValue(boolean value) {
        int boundValue = BOUND_MEMORY.getValueAt(BOUND_ADDRESS);

        int bitMask = 1 << BOUND_BIT;
        boolean oldValue = (boundValue & bitMask) != 0;

        if (value) boundValue |= 1 << BOUND_BIT;
        else boundValue &= ~bitMask;

        BOUND_MEMORY.setValueAt(BOUND_ADDRESS, boundValue);

        return oldValue;
    }
}
