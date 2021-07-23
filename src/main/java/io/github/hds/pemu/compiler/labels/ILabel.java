package io.github.hds.pemu.compiler.labels;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ILabel {

    public static final int NULL_PTR = -1;

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
     * Returns if this label's pointer isn't equal to NULL_PTR
     * @return If this label's pointer isn't equal to NULL_PTR
     */
    default boolean hasPointer() {
        return getPointer() != NULL_PTR;
    }

    /**
     * Sets the position of where this label's last instance was used
     * This is used by the Compiler to know where the last instance of a
     * label was used at, to give better errors if needed.
     * @param file The file where the label was parsed in
     * @param line The line at which the label was parsed at
     * @param character The character at which the label was parsed at
     * @return A reference to this
     */
    @NotNull ILabel setLastInstance(@Nullable File file, int line, int character);

    /**
     * Removes the saved location of the last label's instance
     * @return A reference to this
     */
    default @NotNull ILabel removeLastInstance() {
        return setLastInstance(null, -1, -1);
    }

    /**
     * Returns the last file where this label was instantiated or null if unknown
     * @return The last file where this label was instantiated or null if unknown
     */
    @Nullable File getLastInstanceFile();

    /**
     * Returns the line at which this label was last instantiated or -1 if unknown
     * @return The line at which this label was last instantiated or -1 if unknown
     */
    int getLastInstanceLine();

    /**
     * Returns the line's character at which this label was last instantiated or -1 if unknown
     * @return The line's character at which this label was last instantiated or -1 if unknown
     */
    int getLastInstanceChar();

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
