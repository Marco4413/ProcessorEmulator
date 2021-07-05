package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.*;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.memory.flags.*;
import io.github.hds.pemu.memory.registers.*;
import io.github.hds.pemu.utils.IStoppable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * An interface that holds all basic methods that
 * a Processor should implement to work with PEMU
 */
public interface IProcessor extends Runnable, IStoppable {

    /**
     * Returns the {@link IFlag} with the specified short name or null if not present
     * @param shortName The name of the {@link IFlag} that is being requested
     * @return The specified {@link IFlag} or null if not present
     */
    @Nullable IFlag getFlag(@NotNull String shortName);

    /**
     * Returns the {@link IRegister} with the specified short name or null if not present
     * @param shortName The name of the {@link IRegister} that is being requested
     * @return The specified {@link IRegister} or null if not present
     */
    @Nullable IRegister getRegister(@NotNull String shortName);

    /**
     * Returns the {@link IMemory} that this {@link IProcessor} is using to store data
     * @return The {@link IMemory} used by this {@link IProcessor}
     */
    @NotNull IMemory getMemory();

    /**
     * Returns the {@link Clock} used by this {@link IProcessor} to execute instructions
     * @return The {@link Clock} used by this {@link IProcessor}
     */
    @NotNull Clock getClock();

    /**
     * Returns the {@link InstructionSet} used by this {@link IProcessor} to store all {@link Instruction}s
     * @return The {@link InstructionSet} used by this {@link IProcessor}
     */
    @NotNull InstructionSet getInstructionSet();

    /**
     * Returns the currently pressed key on the keyboard (Set by {@link IProcessor#setKeyPressed}).
     * If no key is being pressed then {@link KeyEvent#VK_UNDEFINED} should be returned.
     * Constants from the class {@link KeyEvent} should be used.
     * @return Returns the currently pressed key
     */
    int getKeyPressed();

    /**
     * Sets the currently pressed key on the keyboard to the one specified (Can be get from {@link IProcessor#getKeyPressed}).
     * If no key is being pressed then {@link KeyEvent#VK_UNDEFINED} should be set.
     * Constants from the class {@link KeyEvent} should be used.
     * @param key The key that is being pressed
     */
    void setKeyPressed(int key);

    /**
     * Returns the currently pressed char on the keyboard.
     * If none or an invalid one is pressed then 0 is returned.
     * @return The currently pressed char on the keyboard
     */
    char getCharPressed();

    /**
     * Sets the currently pressed char on the keyboard.
     * If none or an invalid one is pressed then 0 should be set.
     * @param ch The character that is currently being pressed on the keyboard
     */
    void setCharPressed(char ch);

    /**
     * Returns info about this {@link IProcessor}.
     * Info includes: Clock, Memory Size, Number of Instructions...
     * @return Info about this {@link IProcessor}
     */
    @NotNull String getInfo();

    /**
     * Returns the history of {@link Instruction}s executed by this {@link IProcessor}
     * in the following key-value format: address-shortName.
     * Can return null if not implemented.
     * @return The history of {@link Instruction}s executed by this {@link IProcessor}
     */
    @Nullable HashMap<Integer, String> getInstructionHistory();

    /**
     * Returns how many milliseconds elapsed since this {@link IProcessor} started running for the first time
     * @return How many milliseconds elapsed since this {@link IProcessor} started running
     */
    long getTimeRunning();

    /**
     * Loads the specified program into this {@link IProcessor}'s {@link IMemory}
     * @param program The program to be loaded
     * @return The error message or null if none
     */
    @Nullable String loadProgram(int[] program);

    /**
     * The address at which the first word of the loaded program is at
     * @return The address where the program is stored
     */
    int getProgramAddress();

    /**
     * Words in this {@link IProcessor}'s {@link IMemory} that are reserved for
     * {@link IMemoryRegister}s, {@link IMemoryFlag}s, ...
     * @return Words that are reserved by this {@link IProcessor} in {@link IMemory}
     */
    int getReservedWords();

    /**
     * Runs this {@link IProcessor} and starts its program execution
     * @throws Exception If any error was encountered while running
     */
    @Override
    void run();

    /**
     * Stops this {@link IProcessor}
     */
    @Override
    void stop();

    /**
     * Returns whether or not this {@link IProcessor} is running
     * @return Whether or not this {@link IProcessor} is running
     */
    boolean isRunning();

    /**
     * Returns whether or not this {@link IProcessor} was paused
     * @return Whether or not this {@link IProcessor} was paused
     */
    boolean isPaused();

    /**
     * Pauses this {@link IProcessor}'s execution
     */
    void pause();

    /**
     * Resumes this {@link IProcessor}'s execution
     */
    void resume();

    /**
     * If this {@link IProcessor} was paused then this will execute the next {@link Instruction}
     */
    void step();

}
