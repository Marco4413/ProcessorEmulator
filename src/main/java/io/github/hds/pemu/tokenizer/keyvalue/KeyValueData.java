package io.github.hds.pemu.tokenizer.keyvalue;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public final class KeyValueData {
    protected final HashMap<String, Object> ENTRIES;

    public KeyValueData() {
        this(new HashMap<>());
    }

    public KeyValueData(@NotNull KeyValueData data) {
        this(data.ENTRIES);
    }

    public KeyValueData(@NotNull HashMap<String, Object> entries) {
        ENTRIES = new HashMap<>(entries);
    }

    public void forEach(@NotNull BiConsumer<String, Object> consumer) {
        ENTRIES.forEach(consumer);
    }

    public <T> @Nullable T get(@NotNull Class<T> clazz, @NotNull String key) {
        return getOrDefault(clazz, key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getOrDefault(@NotNull Class<T> clazz, @NotNull String key, T defaultValue) {
        if (!containsKey(key)) return defaultValue;

        Object value = ENTRIES.get(key);
        if (clazz.isAssignableFrom(value.getClass())) return clazz.cast(value);
        else if (Number.class.isAssignableFrom(clazz) && Number.class.isAssignableFrom(value.getClass())) {
            // If we've got a Number and we're trying to convert it to another Number
            //  I'm sure there's a better way of handling this, though this is the easiest way to do it I guess
            Number number = (Number) value;
            if (clazz.equals(Double.class))
                return (T) (Double) number.doubleValue();
            else if (clazz.equals(Float.class))
                return (T) (Float) number.floatValue();
            else if (clazz.equals(Long.class))
                return (T) (Long) number.longValue();
            else if (clazz.equals(Integer.class))
                return (T) (Integer) number.intValue();
            else if (clazz.equals(Short.class))
                return (T) (Short) number.shortValue();
            else if (clazz.equals(Byte.class))
                return (T) (Byte) number.byteValue();
        }
        return defaultValue;
    }

    public void put(@NotNull String key, @NotNull Object value) {
        ENTRIES.put(key, value);
    }

    public boolean containsKey(@NotNull String key) {
        return ENTRIES.containsKey(key);
    }

    private static @NotNull String escapeString(@NotNull String str, char quote, char escapeChar) {
        String escapedStr = StringUtils.SpecialCharacters.escapeAll(str, escapeChar).replace(
                String.valueOf(quote),
                String.valueOf(escapeChar) + quote
        );

        return quote + escapedStr + quote;
    }

    protected @NotNull String objectToString(@Nullable Object object) {
        if (object == null) return "";
        if (object instanceof String)
            return escapeString((String) object, KeyValueParser.STRING_QUOTE, KeyValueParser.ESCAPE_CHARACTER);
        else if (object instanceof Character)
            return escapeString(String.valueOf((Character) object), KeyValueParser.CHAR_QUOTE, KeyValueParser.ESCAPE_CHARACTER);
        else if (object instanceof Number || object instanceof Boolean)
            return object.toString();
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        ENTRIES.forEach(
                (k, v) -> {
                    strBuilder.append('"').append(
                            escapeString(k, KeyValueParser.STRING_QUOTE, KeyValueParser.ESCAPE_CHARACTER)
                    ).append(" = ").append(objectToString(v)).append('\n');
                }
        );

        return strBuilder.toString();
    }
}
