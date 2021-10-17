package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionHistory;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.memory.flags.*;
import io.github.hds.pemu.memory.registers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.List;

public final class DummyProcessor implements IDummyProcessor {

    private final RegisterHolder<IRegister> REGISTER_HOLDER;
    private final FlagHolder<IFlag> FLAG_HOLDER;

    private final DummyMemory MEMORY;
    private final Clock CLOCK;
    private final InstructionSet INSTRUCTIONSET;

    /**
     * Creates a new {@link DummyProcessor}'s instance
     * @param instructionSet The {@link InstructionSet} to be used by the new instance
     * @param config The {@link ProcessorConfig} to create the new instance with
     */
    public DummyProcessor(@NotNull ProcessorConfig config, @NotNull InstructionSet instructionSet) {
        this(config, instructionSet, new IDummyRegister[0], new IDummyFlag[0]);
    }

    /**
     * Creates a new {@link DummyProcessor}'s instance
     * @param config The {@link ProcessorConfig} to create the new instance with
     * @param instructionSet The {@link InstructionSet} to be used by the new instance
     * @param registers The {@link IRegister}s to add to this {@link IProcessor}
     * @param flags The {@link IFlag}s to add to this {@link IProcessor}
     */
    public DummyProcessor(@NotNull ProcessorConfig config, @NotNull InstructionSet instructionSet, @NotNull IDummyRegister[] registers, @NotNull IDummyFlag[] flags) {
        REGISTER_HOLDER = new RegisterHolder<>(registers);
        FLAG_HOLDER = new FlagHolder<>(flags);

        MEMORY = new DummyMemory(Word.getClosestWord(config.getBits()));
        CLOCK = new Clock(config.getClockFrequency());
        INSTRUCTIONSET = instructionSet;
    }

    /**
     * Creates a new {@link DummyProcessor}'s instance
     * @param config The {@link ProcessorConfig} to create the new instance with
     * @param instructionSet The {@link InstructionSet} to be used by the new instance
     * @param registers The {@link IRegister}s to add to this {@link IProcessor}
     * @param flags The {@link IFlag}s to add to this {@link IProcessor}
     */
    public DummyProcessor(@NotNull ProcessorConfig config, @NotNull InstructionSet instructionSet, @NotNull List<IDummyRegister> registers, @NotNull List<IDummyFlag> flags) {
        this(config, instructionSet, registers.toArray(new IDummyRegister[0]), flags.toArray(new IDummyFlag[0]));
    }

    @Override
    public @NotNull IFlag[] getFlags() {
        return FLAG_HOLDER.toArray();
    }

    @Override
    public @NotNull IRegister[] getRegisters() {
        return REGISTER_HOLDER.toArray();
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
    public @Nullable InstructionHistory getInstructionHistory() {
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
