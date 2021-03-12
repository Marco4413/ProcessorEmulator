package io.github.hds.pemu.memory;

public class Memory {

    public static int MAX_UNSIGNED_BYTE = (int) Math.pow(2, Byte.SIZE);

    private final byte[] MEMORY;

    public Memory() {
        this(MAX_UNSIGNED_BYTE);
    }

    public Memory(int size) {
        MEMORY = new byte[size];
    }

    public String toString() {
        return toString(8);
    }

    public String toString(int width) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MEMORY.length; i++) {
            builder.append(Byte.toUnsignedInt(MEMORY[i])).append('\t');

            if ((i + 1) % width == 0) builder.append('\n');
        }
        return builder.toString();
    }

    public int getSize() { return MEMORY.length; }

    public void validateAddress(int address) {
        if (address < 0 || address >= MEMORY.length)
            throw new IllegalArgumentException("Address must be within memory size!");
    }

    public int setValueAt(int address, int value) {
        validateAddress(address);
        int oldValue = getValueAt(address);
        MEMORY[address] = (byte) value;
        return oldValue;
    }

    public int getValueAt(int address) {
        validateAddress(address);
        return Byte.toUnsignedInt(MEMORY[address]);
    }

    public int[] setValuesAt(int address, int[] values) {
        validateAddress(address);
        validateAddress(address + values.length - 1);

        int[] oldValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            oldValues[i] = getValueAt(address + i);
            MEMORY[address + i] = (byte) values[i];
        }
        return oldValues;
    }

    public int[] getValuesAt(int address, int size) {
        validateAddress(address);
        validateAddress(address + size - 1);

        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = getValueAt(address + i);
        }
        return values;
    }
}
