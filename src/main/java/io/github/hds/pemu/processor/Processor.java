package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionError;
import io.github.hds.pemu.instructions.InstructionHistory;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.localization.Translation;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.memory.flags.*;
import io.github.hds.pemu.memory.registers.*;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;

public final class Processor implements IProcessor {

    private boolean isRunning = false;

    // How many words the registers will occupy
    private final int REGISTERS_WORDS = 2;
    // How many words are reserved for the stack
    private final int RESERVED_STACK_ELEMENTS = 1;
    // How many words are reserved for flags
    private final int FLAGS_WORDS = 1;

    private final RegisterHolder<MemoryRegister> REGISTERS;
    private final FlagHolder<MemoryFlag> FLAGS;

    private final Memory MEMORY;
    private final Clock CLOCK;

    private final InstructionSet INSTRUCTIONSET;
    private final InstructionHistory HISTORY;

    private volatile char charPressed = '\0';
    private volatile int keyPressed = KeyEvent.VK_UNDEFINED;
    private long startTimestamp = 0;

    private volatile boolean isPaused = false;
    private volatile boolean stepping = false;

    public Processor(@NotNull ProcessorConfig config) {
        MEMORY = new Memory(
                config.getMemorySize(),
                Word.getClosestWord(config.getBits())
        );
        CLOCK = new Clock(config.getClockFrequency());

        INSTRUCTIONSET = config.getInstructionSet();
        HISTORY = new InstructionHistory();

        REGISTERS = new RegisterHolder<>(
                new MemoryRegister(getProgramAddress(), "Instruction Pointer", MEMORY, 0),
                new MemoryRegister(MEMORY.getSize() - 1, "Stack Pointer", MEMORY, 1)
        );

        FLAGS = new FlagHolder<>(
                new MemoryFlag(false, "Zero Flag" , MEMORY, REGISTERS_WORDS, 0),
                new MemoryFlag(false, "Carry Flag", MEMORY, REGISTERS_WORDS, 1)
        );
    }

    public static @NotNull DummyProcessor getDummyProcessor(@NotNull ProcessorConfig config) {
        return new DummyProcessor(
                config,
                new IDummyRegister[] {
                        new DummyMemoryRegister("Instruction Pointer"),
                        new DummyMemoryRegister("Stack Pointer")
                },
                new IDummyFlag[] {
                        new DummyMemoryFlag("Zero Flag"),
                        new DummyMemoryFlag("Carry Flag"),
                }
        );
    }

    @Override
    public @NotNull IFlag[] getFlags() {
        return FLAGS.toArray();
    }

    @Override
    public @NotNull IRegister[] getRegisters() {
        return REGISTERS.toArray();
    }

    @Override
    public @Nullable IFlag getFlag(@NotNull String shortName) {
        return FLAGS.getFlag(shortName);
    }

    @Override
    public @Nullable IRegister getRegister(@NotNull String shortName) {
        return REGISTERS.getRegister(shortName);
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
        return keyPressed;
    }

    @Override
    public void setKeyPressed(int key) {
        keyPressed = key;
    }

    @Override
    public char getCharPressed() {
        return charPressed;
    }

    @Override
    public void setCharPressed(char ch) {
        charPressed = ch;
    }

    @Override
    public @NotNull String getInfo() {
        Translation currentTranslation = TranslationManager.getCurrentTranslation();
        return StringUtils.format(
                ("\t{0}:\t" + StringUtils.getEngNotation(CLOCK.getFrequency(), "Hz") + "\n" +
                 "\t{1}:\t" + MEMORY.getSize() + 'x' + MEMORY.getWord().TOTAL_BYTES + " Bytes\n" +
                 "\t{2}:\t" + INSTRUCTIONSET.getSize() + "\n"),
                currentTranslation.getOrDefault("messages.clock"),
                currentTranslation.getOrDefault("messages.memory"),
                currentTranslation.getOrDefault("messages.instructions")
        );
    }

    @Override
    public @Nullable InstructionHistory getInstructionHistory() {
        return HISTORY;
    }

    @Override
    public long getTimeRunning() {
        if (!isRunning) return -1;
        return System.currentTimeMillis() - startTimestamp;
    }

    @Override
    public @Nullable String loadProgram(int[] program) {
        if (program.length > MEMORY.getSize() - getReservedWords())
            return TranslationManager.getCurrentTranslation().getOrDefault("messages.processorOutOfMemory");

        MEMORY.setValuesAt(getProgramAddress(), program);
        return null;
    }

    @Override
    public int getProgramAddress() {
        return REGISTERS_WORDS + FLAGS_WORDS;
    }

    @Override
    public int getReservedWords() {
        return REGISTERS_WORDS + FLAGS_WORDS + RESERVED_STACK_ELEMENTS;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void run() {
        if (isRunning) return;

        startTimestamp = System.currentTimeMillis();
        isRunning = true;
        while (isRunning) {
            if (CLOCK.update() && (stepping || !isPaused)) {
                stepping = false;

                final MemoryRegister IP = REGISTERS.getRegister("IP");
                assert IP != null : "Come on, it can't be null...";

                if (IP.getValue() >= MEMORY.getSize()) {
                    stop();
                } else {
                    int currentIP = IP.getValue();
                    Instruction instruction = INSTRUCTIONSET.getInstruction(MEMORY.getValueAt(currentIP));
                    if (instruction == null) throw new InstructionError("Unknown", "Unknown Instruction", currentIP);
                    HISTORY.put(currentIP, instruction.getKeyword());

                    IP.setValue(currentIP + instruction.getWords());
                    try {
                        instruction.execute(
                                this,
                                // Here we check instruction.ARGUMENTS == 0 because Memory#getValuesAt always throws if the address is out of bounds
                                instruction.getArgumentsCount() == 0 ? new int[0] : MEMORY.getValuesAt(currentIP + 1, instruction.getArgumentsCount())
                        );
                    } catch (Exception err) {
                        throw new InstructionError(instruction.getKeyword(), err.getMessage(), currentIP);
                    }
                }
            }
        }
    }

    @Override
    public void stop() { isRunning = false; }

    @Override
    public boolean isPaused() {
        return this.isPaused;
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused = false;
    }

    @Override
    public void step() {
        stepping = true;
    }

}
