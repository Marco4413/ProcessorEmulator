package io.github.hds.pemu.compiler.labels;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

/**
 * A BasicLabel is an implementation of ILabel
 * where its Instances can't have offsets
 */
public class BasicLabel implements ILabel {
    protected int pointer = ILabel.NULL_PTR;
    protected final HashMap<Integer, Integer> INSTANCES = new HashMap<>();

    protected File instanceFile = null;
    protected int instanceLine = -1;
    protected int instanceChar = -1;

    public BasicLabel() { }

    @Override
    public @NotNull BasicLabel setPointer(int pointer) {
        this.pointer = pointer;
        return this;
    }

    @Override
    public int getPointer() {
        return this.pointer;
    }

    @Override
    public boolean hasPointer() {
        return this.pointer != NULL_PTR;
    }

    @Override
    public @NotNull BasicLabel setInstanceLocation(@Nullable File file, int line, int character) {
        instanceFile = file;
        instanceLine = line;
        instanceChar = character;
        return this;
    }

    @Override
    public @NotNull BasicLabel removeInstanceLocation() {
        return setInstanceLocation(null, -1, -1);
    }

    @Override
    public @Nullable File getInstanceFile() {
        return instanceFile;
    }

    @Override
    public int getInstanceLine() {
        return instanceLine;
    }

    @Override
    public int getInstanceChar() {
        return instanceChar;
    }

    @Override
    public @NotNull BasicLabel addInstance(int address) {
        INSTANCES.put(address, 0);
        return this;
    }

    @Override
    public @NotNull Integer[] getInstances() {
        return this.INSTANCES.keySet().toArray(new Integer[0]);
    }

    @Override
    public int getInstancesCount() {
        return this.INSTANCES.size();
    }

    @Override
    public boolean hasInstance(int address) {
        return INSTANCES.containsKey(address);
    }

    @Override
    public int getPointerForInstance(int address) {
        return INSTANCES.containsKey(address) ? this.pointer : ILabel.NULL_PTR;
    }
}
