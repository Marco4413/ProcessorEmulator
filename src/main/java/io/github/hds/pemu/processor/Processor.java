package io.github.hds.pemu.processor;

import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.Flag;
import io.github.hds.pemu.memory.Memory;
import io.github.hds.pemu.memory.Registry;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Processor implements Runnable {

    private boolean isRunning = false;

    public final Registry IP = new Registry("Instruction Pointer");
    public final Registry SP = new Registry("Stack Pointer");

    public final Flag ZERO  = new Flag(false, "Zero Bit");
    public final Flag CARRY = new Flag(false, "Carry Bit");

    public final Memory MEMORY;
    public final Clock CLOCK;

    public final InstructionSet INSTRUCTIONSET;
    public final HashMap<Integer, String> HISTORY;

    public volatile char pressedChar = '\0';
    public volatile int pressedKey = KeyEvent.VK_UNDEFINED;
    private long startTimestamp = 0;

    private boolean isPaused = false;
    private boolean stepping = false;

    public Processor(@NotNull ProcessorConfig config) {
        Word word = new Word(config.bits);
        MEMORY = new Memory(config.memSize, word);
        CLOCK = new Clock(config.clock);

        SP.value = MEMORY.getSize() - 1;

        INSTRUCTIONSET = config.instructionSet;
        HISTORY = new HashMap<>();
    }

    public void updateFlags(int value, boolean zero, boolean carry) {
        if (zero) ZERO.value = value == 0;
        if (carry) CARRY.value = (value & ~MEMORY.WORD.MASK) != 0;
    }

    public @NotNull String getInfo() {
        return "\tMemory:\t" + MEMORY.getSize() + 'x' + MEMORY.WORD.BYTES + " Bytes\n" +
               "\tInstructions:\t" + INSTRUCTIONSET.getSize() + "\n";
    }

    public long getTimeRunning() {
        if (!isRunning) return -1;
        return System.currentTimeMillis() - startTimestamp;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

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

    public void stop() { isRunning = false; }

    public boolean isPaused() {
        return this.isPaused;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void step() {
        stepping = true;
    }

}
