package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterData extends HashMap<Integer, String> {
    protected RegisterData(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    protected RegisterData(int initialCapacity) {
        super(initialCapacity);
    }

    protected RegisterData() {
        super();
    }

    protected RegisterData(Map<? extends Integer, ? extends String> m) {
        super(m);
    }

    @Nullable String getRegisterOnLine(int line) {
        return this.get(line);
    }

    boolean hasRegisterOnLine(int line) {
        return this.containsKey(line);
    }

    @NotNull Integer[] getRegisterLines(@NotNull String registerName) {
        ArrayList<Integer> lines = new ArrayList<>();

        this.forEach((line, name) -> {
            if (name.equals(registerName))
                lines.add(line);
        });

        return lines.toArray(new Integer[0]);
    }

    boolean hasRegister(@NotNull String registerName) {
        return this.containsValue(registerName);
    }
}
