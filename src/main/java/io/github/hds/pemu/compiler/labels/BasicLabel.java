package io.github.hds.pemu.compiler.labels;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * A BasicLabel is an implementation of ILabel
 * where its Instances can't have offsets
 */
public class BasicLabel implements ILabel {
    protected int pointer = ILabel.NULL_PTR;
    protected final HashMap<Integer, Integer> INSTANCES = new HashMap<>();

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
    public @NotNull BasicLabel addInstance(int address) {
        INSTANCES.put(address, 0);
        return this;
    }

    @Override
    public @NotNull Integer[] getInstances() {
        return this.INSTANCES.keySet().toArray(new Integer[0]);
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
