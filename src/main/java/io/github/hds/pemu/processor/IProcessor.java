package io.github.hds.pemu.processor;

import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import io.github.hds.pemu.utils.IStoppable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface IProcessor extends Runnable, IStoppable {

    @Nullable Flag getFlag(@NotNull String shortName);
    @Nullable Registry getRegistry(@NotNull String shortName);
    @NotNull Memory getMemory();
    @NotNull Clock getClock();

    int getKeyPressed();
    void setKeyPressed(int key);

    char getCharPressed();
    void setCharPressed(char ch);

    @NotNull String getInfo();
    @Nullable HashMap<Integer, String> getInstructionHistory();
    long getTimeRunning();

    boolean isRunning();
    boolean isPaused();
    void pause();
    void resume();
    void step();

}
