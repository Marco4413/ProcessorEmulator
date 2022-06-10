package io.github.marco4413.pemu.memory.registers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A basic interface for all Registers
 */
public interface IRegister {

    /**
     * Returns the full name of this Register, in case there isn't one it returns null
     * @return The full name of this Register or null if not present
     */
    @Nullable String getFullName();

    /**
     * Returns the short name of this Register, this should always exist
     * @return The short name of this Register
     */
    @NotNull String getShortName();

    /**
     * Returns the value of this Register
     * @return The value of this Register
     */
    int getValue();

    /**
     * Sets this Register's value to the one specified
     * @param value The new value of this Register
     * @return The old value of this Register
     */
    int setValue(int value);

}
