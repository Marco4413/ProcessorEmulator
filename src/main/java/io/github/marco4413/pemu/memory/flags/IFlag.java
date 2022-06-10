package io.github.marco4413.pemu.memory.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A basic interface for all Flags
 */
public interface IFlag {

    /**
     * Returns the full name of this Flag, in case there isn't one it returns null
     * @return The full name of this Flag or null if not present
     */
    @Nullable String getFullName();

    /**
     * Returns the short name of this Flag, this should always exist
     * @return The short name of this Flag
     */
    @NotNull String getShortName();

    /**
     * Returns the value of this Flag
     * @return Whether or not this Flag is set to 1
     */
    boolean getValue();

    /**
     * Sets this Flag's value to either true or false
     * @param value The new state of this Flag
     * @return The old value of this Flag
     */
    boolean setValue(boolean value);

    /**
     * Sets this Flag's value to either 1 - true or 0 - false
     * @param value The new state of this Flag
     * @return The old value of this Flag
     */
    default int setValue(int value) {
        return this.setValue(value != 0) ? 1 : 0;
    }

}
