package io.github.hds.pemu.compiler;

import java.util.HashMap;
import java.util.Map;

public class LabelData extends HashMap<String, Label> {
    protected LabelData(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    protected LabelData(int initialCapacity) {
        super(initialCapacity);
    }

    protected LabelData() {
        super();
    }

    protected LabelData(Map<? extends String, ? extends Label> m) {
        super(m);
    }
}
