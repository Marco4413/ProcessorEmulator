package io.github.marco4413.pemu.memory.flags;

import io.github.marco4413.pemu.memory.IMemory;
import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MemoryFlag extends AbstractFlag implements IMemoryFlag {

    private final @NotNull IMemory BOUND_MEMORY;
    private final int BOUND_ADDRESS;
    private final int BOUND_BIT;

    public MemoryFlag(boolean value, @NotNull String fullName, @NotNull IMemory boundMemory, int boundAddress, int boundBit) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress, boundBit);
    }

    public MemoryFlag(boolean value, @Nullable String fullName, @NotNull String shortName, @NotNull IMemory boundMemory, int boundAddress, int boundBit) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        BOUND_ADDRESS = boundAddress;
        BOUND_BIT = boundBit;

        setValue(value);
    }

    @Override
    public int getAddress() {
        return BOUND_ADDRESS;
    }

    @Override
    public int getBit() {
        return BOUND_BIT;
    }

    @Override
    public synchronized boolean getValue() {
        int boundValue = BOUND_MEMORY.getValueAt(BOUND_ADDRESS);
        return ( boundValue & (1 << BOUND_BIT) ) != 0;
    }

    @Override
    public synchronized boolean setValue(boolean value) {
        int boundValue = BOUND_MEMORY.getValueAt(BOUND_ADDRESS);

        int bitMask = 1 << BOUND_BIT;
        boolean oldValue = (boundValue & bitMask) != 0;

        if (value) boundValue |= 1 << BOUND_BIT;
        else boundValue &= ~bitMask;

        BOUND_MEMORY.setValueAt(BOUND_ADDRESS, boundValue);

        return oldValue;
    }
}
