package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import io.github.hds.pemu.memory.Word;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Processor implements IProcessor {

    private boolean isRunning = false;

    private final Registry IP = new Registry("Instruction Pointer");
    private final Registry SP = new Registry("Stack Pointer");

    private final Flag ZERO  = new Flag(false, "Zero Flag");
    private final Flag CARRY = new Flag(false, "Carry Flag");

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

        SP.value = MEMORY.getSize() - 1;

        INSTRUCTIONSET = config.instructionSet;
        HISTORY = new HashMap<>();
    }

    @Override
    public @Nullable Flag getFlag(@NotNull String shortName) {
        if (shortName.equals(ZERO.SHORT)) return ZERO;
        else if (shortName.equals(CARRY.SHORT)) return CARRY;
        return null;
    }

    @Override
    public @Nullable Registry getRegistry(@NotNull String shortName) {
        if (shortName.equals(IP.SHORT)) return IP;
        else if (shortName.equals(SP.SHORT)) return SP;
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
                int lastIP = IP.value;

                InstructionSet.ExecutionData executionData = INSTRUCTIONSET.parseAndExecute(this, MEMORY, IP.value);
                HISTORY.put(lastIP, executionData.INSTRUCTION.KEYWORD);
                if (!executionData.IGNORE_LENGTH)
                    IP.value += executionData.LENGTH;

                if (IP.value >= MEMORY.getSize()) stop();
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
