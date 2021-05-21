package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.*;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Processor implements IProcessor {

    private boolean isRunning = false;

    private final int REGISTRIES_WORDS = 2;
    private final BasicRegister IP;
    private final BasicRegister SP;

    private final int FLAGS_WORDS = 1;
    private final MemFlag ZERO;
    private final MemFlag CARRY;

    private final Memory MEMORY;
    private final Clock CLOCK;

    private final InstructionSet INSTRUCTIONSET;
    private final HashMap<Integer, String> HISTORY;

    private volatile char charPressed = '\0';
    private volatile int keyPressed = KeyEvent.VK_UNDEFINED;
    private long startTimestamp = 0;

    private volatile boolean isPaused = false;
    private volatile boolean stepping = false;

    public Processor(@NotNull ProcessorConfig config) {
        Word word = new Word(config.getBits());
        MEMORY = new Memory(config.getMemorySize(), word);
        CLOCK = new Clock(config.getClock());

        INSTRUCTIONSET = config.instructionSet;
        HISTORY = new HashMap<>();

        int lastAddress = MEMORY.getSize() - 1;
        IP = new MemRegister(0, "Instruction Pointer", "IP", MEMORY, lastAddress);
        SP = new MemRegister(lastAddress - (REGISTRIES_WORDS + FLAGS_WORDS), "Stack Pointer", "SP", MEMORY, lastAddress - 1);

        ZERO  = new MemFlag(false, "Zero Flag", MEMORY, lastAddress - REGISTRIES_WORDS, 0);
        CARRY = new MemFlag(false, "Carry Flag", MEMORY, lastAddress - REGISTRIES_WORDS, 1);
    }

    @Override
    public @Nullable BasicFlag getFlag(@NotNull String shortName) {
        if (shortName.equals(ZERO.getShortName())) return ZERO;
        else if (shortName.equals(CARRY.getShortName())) return CARRY;
        return null;
    }

    @Override
    public @Nullable BasicRegister getRegistry(@NotNull String shortName) {
        if (shortName.equals(IP.getShortName())) return IP;
        else if (shortName.equals(SP.getShortName())) return SP;
        return null;
    }

    @Override
    public @NotNull Memory getMemory() {
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
        return "\tClock:\t" + StringUtils.getEngNotationInt(CLOCK.getClock()) + "Hz\n" +
               "\tMemory:\t" + MEMORY.getSize() + 'x' + MEMORY.WORD.BYTES + " Bytes\n" +
               "\tInstructions:\t" + INSTRUCTIONSET.getSize() + "\n";
    }

    @Override
    public @Nullable HashMap<Integer, String> getInstructionHistory() {
        return HISTORY;
    }

    @Override
    public long getTimeRunning() {
        if (!isRunning) return -1;
        return System.currentTimeMillis() - startTimestamp;
    }

    @Override
    public @Nullable String loadProgram(int[] program) {
        if (program.length > MEMORY.getSize() - (getReservedWords()))
            return "Couldn't load program because there's not enough space!";

        MEMORY.setValuesAt(0, program);
        return null;
    }

    @Override
    public int getReservedWords() {
        // The +1 at the end is the first element on the stack
        return REGISTRIES_WORDS + FLAGS_WORDS + 1;
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

                if (IP.getValue() >= MEMORY.getSize()) {
                    stop();
                } else {
                    int currentIP = IP.getValue();
                    Instruction instruction = INSTRUCTIONSET.parse(MEMORY, currentIP);
                    HISTORY.put(currentIP, instruction.KEYWORD);

                    IP.setValue(currentIP + instruction.getWords());
                    instruction.execute(
                            this,
                            instruction.ARGUMENTS == 0 ? new int[0] : MEMORY.getValuesAt(currentIP + 1, instruction.ARGUMENTS)
                    );
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
