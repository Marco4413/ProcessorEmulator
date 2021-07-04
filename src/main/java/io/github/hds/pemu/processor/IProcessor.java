package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.utils.IStoppable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface IProcessor extends Runnable, IStoppable {

    // Are you here because you want to implement this interface?
    //  Note: The run method is the only one that can throw if the processor encounters any error while running
    //    **  Also add/remove Registers/Flags from the static fields of the class DummyProcessor to make the app
    //        verify your programs successfully

    @Nullable IFlag getFlag(@NotNull String shortName);
    @Nullable IRegister getRegister(@NotNull String shortName);
    @NotNull IMemory getMemory();
    @NotNull Clock getClock();
    @NotNull InstructionSet getInstructionSet();

    int getKeyPressed();
    void setKeyPressed(int key);

    char getCharPressed();
    void setCharPressed(char ch);

    @NotNull String getInfo();
    @Nullable HashMap<Integer, String> getInstructionHistory();
    long getTimeRunning();

    @Nullable String loadProgram(int[] program);
    int getProgramAddress();
    int getReservedWords();

    boolean isRunning();
    boolean isPaused();
    void pause();
    void resume();
    void step();

}
