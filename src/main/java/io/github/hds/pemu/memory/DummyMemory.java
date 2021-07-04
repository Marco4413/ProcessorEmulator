package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

public final class DummyMemory implements IMemory {

    /* Not sure if I need to make this Thread Safe... Maybe for consistency with the default Memory? */

    private final @NotNull Word WORD;

    public DummyMemory(@NotNull Word word) {
        WORD = word;
    }

    @Override
    public int getMaxValue() {
        return WORD.BIT_MASK;
    }

    @Override
    public @NotNull Word getWord() {
        return WORD;
    }

    @Override
    public int getSize() {
        return WORD.BIT_MASK + 1;
    }

    @Override
    public boolean isAddressValid(int address) {
        return address >= 0 && address <= WORD.BIT_MASK;
    }

    private void validateAddress(int address) {
        if (!isAddressValid(address))
            throw new NullPointerException("Address (" + address + ") is out of memory!");
    }

    @Override
    public int setValueAt(int address, int value) {
        validateAddress(address);
        return 0;
    }

    @Override
    public int getValueAt(int address) {
        validateAddress(address);
        return 0;
    }

    @Override
    public int[] setValuesAt(int address, int[] values) {
        validateAddress(address);
        validateAddress(address + values.length - 1);
        return new int[values.length];
    }

    @Override
    public int[] getValuesAt(int address, int size) {
        validateAddress(address);
        validateAddress(address + size - 1);
        return new int[size];
    }

}
