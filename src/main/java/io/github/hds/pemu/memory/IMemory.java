package io.github.hds.pemu.memory;

import org.jetbrains.annotations.NotNull;

public interface IMemory {

    /**
     * Returns the max value that this memory can store in an address
     * @return The max value that this memory can store in an address
     */
    int getMaxValue();

    /**
     * Returns the Word instance that this Memory is currently using to store its values
     * @return The Word instance that this Memory is currently using to store its values
     */
    @NotNull Word getWord();

    /**
     * Returns the size (The amount of addresses) this Memory has
     * @return The size of this Memory
     */
    int getSize();

    /**
     * Checks if the specified address is inside Memory
     * @param address The address to test for
     * @return true if the specified address is valid
     */
    boolean isAddressValid(int address);

    /**
     * Sets the value at the specified address to the one specified
     * @throws NullPointerException If the specified address is out of bounds
     * @param address The address to set
     * @param value The value to set at the specified address
     * @return The old value at the specified address
     */
    int setValueAt(int address, int value);

    /**
     * Returns the value at the specified address
     * @throws NullPointerException If the specified address is out of bounds
     * @param address The address to get the value from
     * @return The value at the specified address
     */
    int getValueAt(int address);

    /**
     * Sets the values starting from the specified address to the specified values
     * @throws NullPointerException If an address is out of bounds
     * @param address The address to start setting the values from
     * @param values The values to set from the specified address
     * @return The old values
     */
    int[] setValuesAt(int address, int[] values);

    /**
     * Returns the values starting from the specified address and ending at (address + size - 1)
     * @throws NullPointerException If an address is out of bounds
     * @param address The address to start getting the values from
     * @param size The amount of values to retrieve
     * @return The retrieved values
     */
    int[] getValuesAt(int address, int size);
}
