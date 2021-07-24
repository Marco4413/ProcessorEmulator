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
     * Sets the position of where one of this label's instances was parsed at
     * This is used by the Compiler to know where one instance of this
     * label was used at, to give better errors if needed.
     * @param file The file where the label was parsed in
     * @param line The line at which the label was parsed at
     * @param character The character at which the label was parsed at
     * @return A reference to this
     */
    @NotNull ILabel setInstanceLocation(@Nullable File file, int line, int character);

    /**
     * Removes the saved location of the a label's instance
     * @return A reference to this
     */
    default @NotNull ILabel removeInstanceLocation() {
        return setInstanceLocation(null, -1, -1);
    }

    /**
     * Returns the file where this label was instantiated or null if unknown
     * @return The file where this label was instantiated or null if unknown
     */
    @Nullable File getInstanceFile();

    /**
     * Returns the line at which this label was instantiated or -1 if unknown
     * @return The line at which this label was instantiated or -1 if unknown
     */
    int getInstanceLine();

    /**
     * Returns the line's character at which this label was instantiated or -1 if unknown
     * @return The line's character at which this label was instantiated or -1 if unknown
     */
    int getInstanceChar();

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
     * Returns the amount of instances this label has
     * @return The amount of instances this label has
     */
    int getInstancesCount();

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
