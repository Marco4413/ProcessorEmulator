package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class OffsetsData extends HashMap<Integer, Integer> {

    protected OffsetsData() {
        super();
    }

    public @Nullable Integer getOffsetAtAddress(int address) {
        return get(address);
    }

    public boolean hasOffsetAtAddress(int address) {
        return containsKey(address);
    }

}
