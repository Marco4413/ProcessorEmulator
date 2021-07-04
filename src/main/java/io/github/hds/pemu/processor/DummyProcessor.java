package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.DummyMemory;
import io.github.hds.pemu.memory.IMemory;
import io.github.hds.pemu.memory.Word;
import io.github.hds.pemu.memory.flags.DummyFlag;
import io.github.hds.pemu.memory.flags.FlagHolder;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.registers.DummyRegister;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.memory.registers.RegisterHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public final class DummyProcessor implements IProcessor {

    private static final Clock CLOCK = new Clock(1);

    private final RegisterHolder<IRegister> REGISTER_HOLDER;
    private final FlagHolder<IFlag> FLAG_HOLDER;

    private final DummyMemory MEMORY;
    private final @NotNull InstructionSet INSTRUCTIONSET;

    public DummyProcessor(@NotNull ProcessorConfig config, @NotNull IRegister[] registers, @NotNull IFlag[] flags) {
        REGISTER_HOLDER = new RegisterHolder<>(registers);
        FLAG_HOLDER = new FlagHolder<>(flags);

        MEMORY = new DummyMemory(Word.getClosestWord(config.getBits()));
        INSTRUCTIONSET = config.getInstructionSet();
    }

    @Override
    public @Nullable IFlag getFlag(@NotNull String shortName) {
        return FLAG_HOLDER.getFlag(shortName);
    }

    @Override
    public @Nullable IRegister getRegister(@NotNull String shortName) {
        return REGISTER_HOLDER.getRegister(shortName);
    }

    @Override
    public @NotNull IMemory getMemory() {
        return MEMORY;
    }

    @Override
    public @NotNull Clock getClock() {
        return CLOCK;
    }

    @Override
    public @NotNull InstructionSet getInstructionSet() {
        return INSTRUCTIONSET;
    }

    @Override
    public int getKeyPressed() {
        return KeyEvent.VK_UNDEFINED;
    }

    @Override
    public void setKeyPressed(int key) { }

    @Override
    public char getCharPressed() {
        return '\0';
    }

    @Override
    public void setCharPressed(char ch) { }

    @Override
    public @NotNull String getInfo() {
        return   "This is a DummyProcessor instance, it's just used to\n"
               + " compile programs without allocating useless Memory.\n"
               + "If you're seeing this on the Application then something terrible happened...";
    }

    @Override
    public @Nullable HashMap<Integer, String> getInstructionHistory() {
        return null;
    }

    @Override
    public long getTimeRunning() {
        return 0;
    }

    @Override
    public @Nullable String loadProgram(int[] program) {
        if (program.length > MEMORY.getSize())
            return "Couldn't load program because there's not enough space!";
        return null;
    }

    @Override
    public int getProgramAddress() {
        return 0;
    }

    @Override
    public int getReservedWords() {
        return 0;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void step() { }

    @Override
    public void stop() { }

    @Override
    public void run() { }

}
