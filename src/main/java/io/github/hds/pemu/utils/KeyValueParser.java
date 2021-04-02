package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class KeyValueParser {

    public static class ParsedData {
        private final HashMap<String, Object> ENTRIES;

        protected ParsedData(@NotNull HashMap<String, Object> entries) {
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
    }

    private static final Token STRING = new Token("\"");
    private static final Token CHARACTER = new Token("'");
    private static final Token ESCAPE_CHAR = new Token("\\\\");
    private static final Token ASSIGN = new Token("=");
    private static final Token WHITESPACE = new Token("\\s");

    private static @Nullable String parseString(@NotNull Tokenizer tokenizer) {
        String terminator = tokenizer.peekNext(WHITESPACE);
        if (!STRING.equals(terminator)) return null;
        tokenizer.consumeNext(WHITESPACE);

        StringBuilder builder = new StringBuilder();
        boolean isEscaping = false;

        while (true) {
            String nextToken = tokenizer.consumeNext();
            if (nextToken == null) return null;
            if (isEscaping) {
                char firstChar = nextToken.charAt(0);
                builder.append(StringUtils.SpecialCharacters.MAP.getOrDefault(firstChar, firstChar));
                if (nextToken.length() > 1) builder.append(nextToken.substring(1));
                isEscaping = false;
            } else if (nextToken.equals(terminator)) return builder.toString();
            else if (ESCAPE_CHAR.equals(nextToken)) isEscaping = true;
            else builder.append(nextToken);
        }
    }

    private static @Nullable Character parseCharacter(@NotNull Tokenizer tokenizer) {
        String terminator = tokenizer.peekNext(WHITESPACE);
        if (!CHARACTER.equals(terminator)) return null;
        tokenizer.consumeNext(WHITESPACE);

        String nextToken = tokenizer.consumeNext();
        if (nextToken == null) return null;

        if (ESCAPE_CHAR.equals(nextToken)) {
            String charToEscape = tokenizer.consumeNext();
            if (charToEscape == null || charToEscape.length() > 1) return null;
            if (!terminator.equals(tokenizer.consumeNext())) return null;
            char actualChar = charToEscape.charAt(0);
            return StringUtils.SpecialCharacters.MAP.getOrDefault(actualChar, actualChar);
        } else if (nextToken.length() > 1) return null;
        else if (terminator.equals(tokenizer.consumeNext())) return nextToken.charAt(0);
        return null;
    }

    private static @Nullable Integer parseNumber(@NotNull Tokenizer tokenizer) {
        try {
            return StringUtils.parseInt(tokenizer.consumeNext(WHITESPACE));
        } catch (Exception ignored) { }
        return null;
    }

    public static @NotNull ParsedData parseKeyValuePairs(@NotNull Readable readable) {
        Scanner scanner = new Scanner(readable);

        HashMap<String, Object> entries = new HashMap<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            Tokenizer tokenizer = new Tokenizer(line, true, STRING, CHARACTER, ESCAPE_CHAR, ASSIGN, WHITESPACE);
            tokenizer.removeEmpties();

            String key = parseString(tokenizer);
            if (key == null) continue;

            String nextToken = tokenizer.consumeNext(WHITESPACE);
            if (!ASSIGN.equals(nextToken)) continue;

            String str = parseString(tokenizer);
            if (str != null) {
                entries.put(key, str);
                continue;
            }

            Character character = parseCharacter(tokenizer);
            if (character != null) {
                entries.put(key, character);
                continue;
            }

            Integer number = parseNumber(tokenizer);
            if (number != null) {
                entries.put(key, number);
            }
        }

        return new ParsedData(entries);
    }

}
