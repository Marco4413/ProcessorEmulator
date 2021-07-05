package io.github.hds.pemu.memory.flags;

/**
 * A Flag that is held in Memory
 */
public interface IMemoryFlag extends IFlag {

    /**
     * Returns the Memory address where this Flag is held
     * @return The address at which this Flag is held in Memory
     */
    int getAddress();

    /**
     * Returns the Bit that holds this Flag's value
     * @return The Bit that holds this Flag's value
     */
    int getBit();

}
