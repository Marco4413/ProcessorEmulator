package io.github.hds.pemu.memory.registers;

/**
 * A Register that is held in Memory
 */
public interface IMemoryRegister extends IRegister {

    /**
     * Returns the Memory address where this Register is held
     * @return The address at which this Register is held in Memory
     */
    int getAddress();

}
