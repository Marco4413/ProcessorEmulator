package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.utils.IStoppable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface IProcessor extends Runnable, IStoppable {

    @Nullable IFlag getFlag(@NotNull String shortName);
    @Nullable IRegister getRegistry(@NotNull String shortName);
    @NotNull Memory getMemory();
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
    int getReservedWords();

    boolean isRunning();
    boolean isPaused();
    void pause();
    void resume();
    void step();

}
