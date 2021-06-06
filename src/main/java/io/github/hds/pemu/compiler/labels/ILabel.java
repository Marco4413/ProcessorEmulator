package io.github.hds.pemu.compiler.labels;

import org.jetbrains.annotations.NotNull;

public interface ILabel {

    public static int NULL_PTR = -1;

    /**
     * Sets this Label's pointer to the one specified
     * @param pointer The pointer to set
     * @return A reference to this
     */
    @NotNull ILabel setPointer(int pointer);

    /**
     * Returns the pointer that this label is currently pointing to
     * @return The memory address that this label points to
     */
    int getPointer();

    /**
     * Adds an instance of this label (An address where this label is used in)
     * @param address The address of the instance to add
     * @return A reference to this
     */
    @NotNull ILabel addInstance(int address);

    /**
     * Returns all instances of this label
     * @return All instances of this label (No element should be equal to null)
     */
    @NotNull Integer[] getInstances();

    /**
     * Returns whether or not this Label has an instance at the specified address
     * @param address The address to check
     * @return Whether or not this Label has an instance at the specified address
     */
    boolean hasInstance(int address);

    /**
     * Returns where the instance at the specified address points to
     * @param address The address of the instance that you want to get the pointer from
     * @return Where the specified instance points to
     */
    int getPointerForInstance(int address);

}
