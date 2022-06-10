package io.github.marco4413.pemu.memory.registers;

import io.github.marco4413.pemu.memory.IMemory;
import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MemoryRegister extends AbstractRegister implements IMemoryRegister {

    private final @NotNull IMemory BOUND_MEMORY;
    private final int BOUND_ADDRESS;

    public MemoryRegister(int value, @NotNull String fullName, @NotNull IMemory boundMemory, int boundAddress) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress);
    }

    public MemoryRegister(int value, @Nullable String fullName, @NotNull String shortName, @NotNull IMemory boundMemory, int boundAddress) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        BOUND_ADDRESS = boundAddress;

        setValue(value);
    }

    @Override
    public int getAddress() {
        return BOUND_ADDRESS;
    }

    /* Not sure if this needs to be synchronized since Memory's methods already are */

    @Override
    public synchronized int getValue() {
        return BOUND_MEMORY.getValueAt(BOUND_ADDRESS);
    }

    @Override
    public synchronized int setValue(int value) {
        return BOUND_MEMORY.setValueAt(BOUND_ADDRESS, value);
    }
}
