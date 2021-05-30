package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OffsetsData extends HashMap<Integer, Integer> {

    protected OffsetsData(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    protected OffsetsData(int initialCapacity) {
        super(initialCapacity);
    }

    protected OffsetsData() {
        super();
    }

    protected OffsetsData(Map<? extends Integer, ? extends Integer> m) {
        super(m);
    }

    public @Nullable Integer getOffsetAtAddress(int address) {
        return get(address);
    }

    public boolean hasOffsetAtAddress(int address) {
        return containsKey(address);
    }

}
