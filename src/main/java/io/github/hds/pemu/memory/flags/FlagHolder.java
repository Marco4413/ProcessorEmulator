package io.github.hds.pemu.memory.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public final class FlagHolder<T extends IFlag> {

    private final HashMap<String, T> FLAGS;

    public FlagHolder() {
        FLAGS = new HashMap<>();
    }

    public FlagHolder(T flag) {
        this();
        addFlag(flag);
    }

    @SafeVarargs
    public FlagHolder(T... flags) {
        this();
        addFlags(flags);
    }

    public int getCount() {
        return FLAGS.size();
    }

    public @NotNull IFlag[] toArray() {
        return FLAGS.values().toArray(new IFlag[0]);
    }

    public @NotNull T[] toArray(T[] array) {
        return FLAGS.values().toArray(array);
    }

    public @NotNull FlagHolder<T> addFlag(T flag) {
        String shortName = flag.getShortName();
        if (FLAGS.containsKey(shortName))
            throw new IllegalArgumentException("Flags can't have duplicate short names (" + shortName + ")!");
        FLAGS.put(flag.getShortName(), flag);
        return this;
    }

    @SafeVarargs
    public final @NotNull FlagHolder<T> addFlags(T ...flags) {
        for (T flag : flags) addFlag(flag);
        return this;
    }

    public @Nullable T removeFlag(@NotNull String shortName) {
        return FLAGS.remove(shortName);
    }

    public @Nullable T getFlag(@NotNull String shortName) {
        return FLAGS.get(shortName);
    }

    public @NotNull FlagHolder<T> forEach(@NotNull BiConsumer<String, T> action) {
        FLAGS.forEach(action);
        return this;
    }

}
