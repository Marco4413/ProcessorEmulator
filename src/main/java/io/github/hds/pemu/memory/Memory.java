package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

/**
 * A class that emulates RAM
 */
public final class Memory implements IMemory {

    private final Word WORD;
    private final byte[] MEMORY;

    public Memory(int size, @NotNull Word word) {
        if (size < 0) throw new IllegalArgumentException("Memory size can't be negative!");
        MEMORY = new byte[word.TOTAL_BYTES * size];
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
    public boolean isAddressValid(int address) {
        return address >= 0 && getIndexFromAddress(address) + WORD.TOTAL_BYTES - 1 < MEMORY.length;
    }

    @Override
    public int getSize() { return MEMORY.length / WORD.TOTAL_BYTES; }

    private int getIndexFromAddress(int address) {
        return address * WORD.TOTAL_BYTES;
    }

    private void validateAddress(int address) {
        if (!isAddressValid(address))
            throw new NullPointerException("Address (" + address + ") is out of memory!");
    }

    @Override
    public synchronized int setValueAt(int address, int value) {

        validateAddress(address);
        int oldValue = getValueAt(address);

        int index = getIndexFromAddress(address);
        int[] bytes = WORD.getBytes(value);
        for (int i = 0; i < bytes.length; i++) {
            MEMORY[index + i] = (byte) bytes[i];
        }

        return oldValue;
    }

    @Override
    public synchronized int getValueAt(int address) {
        validateAddress(address);

        int index = getIndexFromAddress(address);
        int[] bytes = new int[WORD.TOTAL_BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.toUnsignedInt(MEMORY[index + i]);
        }

        return WORD.combineBytes(bytes);
    }

    @Override
    public synchronized int[] setValuesAt(int address, int[] values) {
        validateAddress(address);

        if (values.length == 0) return values;
        validateAddress(address + values.length - 1);

        int[] oldValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            oldValues[i] = setValueAt(address + i, values[i]);
        }
        return oldValues;
    }

    @Override
    public synchronized int[] getValuesAt(int address, int size) {
        validateAddress(address);

        if (size == 0) return new int[0];
        validateAddress(address + size - 1);

        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = getValueAt(address + i);
        }
        return values;
    }
}
