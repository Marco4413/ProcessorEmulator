package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A class that emulates RAM
 */
public class Memory {

    /**
     * This class is used with Memory#toString to format the outputted String
     */
    public static class FormatterData {
        public @NotNull StringBuilder builder;
        public int index;
        public int value;

        protected FormatterData(@NotNull StringBuilder builder, int index, int value) {
            this.builder = builder;
            this.index = index;
            this.value = value;
        }
    }

    /**
     * The max value that this Memory can hold in an address
     */
    public final int MAX_VALUE;
    /**
     * Describes how much data each address can hold
     */
    public final Word WORD;

    private final byte[] MEMORY;

    public Memory() {
        this(256);
    }

    public Memory(int size) {
        this(size, new Word(Word.SizeBit8));
    }

    public Memory(int size, Word word) {
        if (size < 0) throw new IllegalArgumentException("Memory size can't be negative!");
        MEMORY = new byte[word.BYTES * size];

        MAX_VALUE = word.MASK;
        WORD = word;
    }

    public synchronized String toString() {
        return toString(true);
    }

    public synchronized String toString(boolean divideByWord) {
        return toString(divideByWord, 8);
    }

    public synchronized String toString(boolean divideByWord, int width) {
        return toString(divideByWord, width, d -> d.builder.append(d.value));
    }

    public synchronized String toString(boolean divideByWord, int width, Consumer<FormatterData> formatter) {
        StringBuilder builder = new StringBuilder();
        int size = divideByWord ? getSize() : MEMORY.length;
        for (int i = 0; i < size; i++) {

            int value = divideByWord ? getValueAt(i) : Byte.toUnsignedInt(MEMORY[i]);
            formatter.accept(new FormatterData(builder, i, value));
            if ((i + 1) % width == 0) builder.append('\n');
            else builder.append('\t');
        }
        return builder.toString();
    }

    /**
     * Returns the size (The amount of addresses) this Memory has
     * @return The size of this Memory
     */
    public int getSize() { return MEMORY.length / WORD.BYTES; }

    private int getIndexFromAddress(int address) {
        return address * WORD.BYTES;
    }

    /**
     * Checks if the specified address is inside Memory
     * @throws IllegalArgumentException If the specified address is out of bounds
     * @param address The address to test for
     */
    public void validateAddress(int address) {
        if (address < 0 || getIndexFromAddress(address) + WORD.BYTES - 1 >= MEMORY.length)
            throw new IllegalArgumentException("Address must be within memory size!");
    }

    /**
     * Sets the value at the specified address to the one specified
     * @param address The address to set
     * @param value The value to set at the specified address
     * @return The old value at the specified address
     */
    public synchronized int setValueAt(int address, int value) {
        int index = getIndexFromAddress(address);

        validateAddress(address);
        int oldValue = getValueAt(address);

        int[] bytes = WORD.getBytes(value);
        for (int i = 0; i < bytes.length; i++) {
            MEMORY[index + i] = (byte) bytes[i];
        }

        return oldValue;
    }

    /**
     * Returns the value at the specified address
     * @param address The address to get the value from
     * @return The value at the specified address
     */
    public synchronized int getValueAt(int address) {
        int index = getIndexFromAddress(address);
        validateAddress(address);

        int[] bytes = new int[WORD.BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.toUnsignedInt(MEMORY[index + i]);
        }

        return WORD.combineBytes(bytes);
    }

    /**
     * Sets the values starting from the specified address to the specified values
     * @param address The address to start setting the values from
     * @param values The values to set from the specified address
     * @return The old values
     */
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

    /**
     * Returns the values starting from the specified address and ending at (address + size - 1)
     * @param address The address to start getting the values from
     * @param size The amount of values to retrieve
     * @return The retrieved values
     */
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
