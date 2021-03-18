package io.github.hds.pemu.memory;

import io.github.hds.pemu.processor.Word;

public class Memory {

    public final int MAX_VALUE;
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
        MEMORY = new byte[size];

        MAX_VALUE = word.MASK;
        WORD = word;
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean divideByWord) {
        return toString(divideByWord, 8);
    }

    public String toString(boolean divideByWord, int width) {
        StringBuilder builder = new StringBuilder();
        int size = divideByWord ? getSize() : MEMORY.length;
        for (int i = 0; i < size; i++) {
            if (divideByWord) builder.append(getValueAt(i));
            else builder.append(Byte.toUnsignedInt(MEMORY[i]));

            if ((i + 1) % width == 0) builder.append('\n');
            else builder.append('\t');
        }
        return builder.toString();
    }

    public int getSize() { return MEMORY.length / WORD.BYTES; }

    private int getIndexFromAddress(int address) {
        return address * WORD.BYTES;
    }

    public void validateAddress(int address) {
        if (address < 0 || getIndexFromAddress(address) + WORD.BYTES - 1 >= MEMORY.length)
            throw new IllegalArgumentException("Address must be within memory size!");
    }

    public int setValueAt(int address, int value) {
        int index = getIndexFromAddress(address);

        validateAddress(address);
        int oldValue = getValueAt(address);

        int[] bytes = WORD.getBytes(value);
        for (int i = 0; i < bytes.length; i++) {
            MEMORY[index + i] = (byte) bytes[i];
        }

        return oldValue;
    }

    public int getValueAt(int address) {
        int index = getIndexFromAddress(address);
        validateAddress(address);

        int[] bytes = new int[WORD.BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.toUnsignedInt(MEMORY[index + i]);
        }

        return WORD.combineBytes(bytes);
    }

    public int[] setValuesAt(int address, int[] values) {
        validateAddress(address);

        if (values.length == 0) return values;
        validateAddress(address + values.length - 1);

        int[] oldValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            oldValues[i] = setValueAt(address + i, values[i]);
        }
        return oldValues;
    }

    public int[] getValuesAt(int address, int size) {
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
