package io.github.hds.pemu.compiler.labels;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * An OffsetLabel extends BasicLabel to add
 * offsets capabilities to its instances
 */
public class OffsetLabel extends BasicLabel {

    public OffsetLabel() { }

    @Override
    public @NotNull OffsetLabel setPointer(int pointer) {
        this.pointer = pointer;
        return this;
    }

    @Override
    public @NotNull OffsetLabel setInstanceLocation(@Nullable File file, int line, int character) {
        super.setInstanceLocation(file, line, character);
        return this;
    }

    @Override
    public @NotNull OffsetLabel removeInstanceLocation() {
        super.removeInstanceLocation();
        return this;
    }

    @Override
    public @NotNull OffsetLabel addInstance(int address) {
        INSTANCES.put(address, 0);
        return this;
    }

    public @NotNull OffsetLabel addInstance(int address, int offset) {
        INSTANCES.put(address, offset);
        return this;
    }

    @Override
    public int getPointerForInstance(int address) {
        return INSTANCES.containsKey(address) ? ( this.pointer + INSTANCES.get(address) ) : ILabel.NULL_PTR;
    }

    public @NotNull OffsetLabel setOffsetForInstance(int address, int offset) {
        if (INSTANCES.containsKey(address)) {
            INSTANCES.put(address, offset);
            return this;
        }
        throw new NullPointerException("No instance at address: " + address);
    }

    public int getOffsetForInstance(int address) {
        if (INSTANCES.containsKey(address)) return INSTANCES.get(address);
        throw new NullPointerException("No offset for instance at address: " + address);
    }
}
