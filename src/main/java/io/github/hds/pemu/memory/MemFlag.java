package io.github.hds.pemu.memory;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MemFlag extends BasicFlag {

    private final @NotNull Memory BOUND_MEMORY;
    private int boundAddress;
    private int boundBit;

    public MemFlag(boolean value, @NotNull String fullName, @NotNull Memory boundMemory, int boundAddress, int boundBit) {
        this(value, fullName, StringUtils.toShortName(fullName), boundMemory, boundAddress, boundBit);
    }

    public MemFlag(boolean value, @NotNull String fullName, @NotNull String shortName, @NotNull Memory boundMemory, int boundAddress, int boundBit) {
        super(fullName, shortName);

        BOUND_MEMORY = boundMemory;
        this.boundAddress = boundAddress;
        this.boundBit = boundBit;

        setValue(value);
    }

    @Override
    public boolean getValue() {
        int boundValue = BOUND_MEMORY.getValueAt(boundAddress);
        return ( boundValue & (1 << boundBit) ) != 0;
    }

    @Override
    public boolean setValue(boolean value) {
        int boundValue = BOUND_MEMORY.getValueAt(boundAddress);

        int bitMask = 1 << boundBit;
        boolean oldValue = (boundValue & bitMask) != 0;

        if (value) boundValue |= 1 << boundBit;
        else boundValue &= ~bitMask;

        BOUND_MEMORY.setValueAt(boundAddress, boundValue);

        return oldValue;
    }
}
