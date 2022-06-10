package io.github.marco4413.pemu.instructions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public final class InstructionHistory {
    private final HashMap<Integer, String> MAP = new HashMap<>();

    public InstructionHistory() { }

    public boolean isEmpty() {
        return MAP.isEmpty();
    }

    public @Nullable String get(int key) {
        return MAP.get(key);
    }

    public boolean containsKey(int key) {
        return MAP.containsKey(key);
    }

    public @Nullable String put(int key, @NotNull String value) {
        return MAP.put(key, value);
    }

    public @Nullable String remove(int key) {
        return MAP.remove(key);
    }

    public void clear() {
        MAP.clear();
    }

    public void forEach(@NotNull BiConsumer<? super Integer, ? super String> action) {
        MAP.forEach(action);
    }
}
