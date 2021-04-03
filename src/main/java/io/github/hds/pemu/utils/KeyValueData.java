package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class KeyValueData {
    private final HashMap<String, Object> ENTRIES;

    public KeyValueData() {
        this(new HashMap<>());
    }

    public KeyValueData(@NotNull HashMap<String, Object> entries) {
        ENTRIES = entries;
    }

    public void forEach(@NotNull BiConsumer<String, Object> consumer) {
        ENTRIES.forEach(consumer);
    }

    public <T> @Nullable T get(@NotNull Class<T> clazz, @NotNull String key) {
        return getOrDefault(clazz, key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getOrDefault(@NotNull Class<T> clazz, @NotNull String key, T defaultValue) {
        Object value = ENTRIES.get(key);
        if (clazz.isInstance(value)) return (T) value;
        return defaultValue;
    }

    public void put(@NotNull String key, @NotNull Object value) {
        ENTRIES.put(key, value);
    }

    public boolean containsKey(@NotNull String key) {
        return ENTRIES.containsKey(key);
    }

    protected @NotNull String ObjectToString(@Nullable Object object) {
        if (object == null) return "";
        if (object instanceof String)
            return "\"" + StringUtils.SpecialCharacters.escapeAll((String) object) + "\"";
        else if (object instanceof Character)
            return "'" + StringUtils.SpecialCharacters.escapeAll((Character) object) + "'";
        else if (object instanceof Number)
            return object.toString();
        return "";
    }

    @Override
    public String toString() {
        StringBuilder thisAsString = new StringBuilder();
        ENTRIES.forEach(
                (k, v) -> {
                    thisAsString.append('"').append(
                            StringUtils.SpecialCharacters.escapeAll(k)
                    ).append("\" = ").append(ObjectToString(v)).append('\n');
                }
        );
        return thisAsString.toString();
    }
}
