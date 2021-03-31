package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class KeyValueParser {

    public static class ParsedData {
        private final HashMap<String, Integer>   NUMBERS   ;
        private final HashMap<String, String>    STRINGS   ;
        private final HashMap<String, Character> CHARACTERS;

        protected ParsedData(@NotNull HashMap<String, Integer> numbers, @NotNull HashMap<String, String> strings, @NotNull HashMap<String, Character> characters) {
            NUMBERS = numbers;
            STRINGS = strings;
            CHARACTERS = characters;
        }

        public void forEachNumber(@NotNull BiConsumer<String, Integer> consumer) {
            NUMBERS.forEach(consumer);
        }

        public @Nullable Integer getNumber(@NotNull String key) {
            return NUMBERS.get(key);
        }

        public void forEachString(@NotNull BiConsumer<String, String> consumer) {
            STRINGS.forEach(consumer);
        }

        public @Nullable String getString(@NotNull String key) {
            return STRINGS.get(key);
        }

        public void forEachCharacter(@NotNull BiConsumer<String, Character> consumer) {
            CHARACTERS.forEach(consumer);
        }

        public @Nullable Character getCharacter(@NotNull String key) {
            return CHARACTERS.get(key);
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

        HashMap<String, Integer>   numbers    = new HashMap<>();
        HashMap<String, String>    strings    = new HashMap<>();
        HashMap<String, Character> characters = new HashMap<>();

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
                strings.put(key, str);
                continue;
            }

            Character character = parseCharacter(tokenizer);
            if (character != null) {
                characters.put(key, character);
                continue;
            }

            Integer number = parseNumber(tokenizer);
            if (number != null) {
                numbers.put(key, number);
            }
        }

        return new ParsedData(numbers, strings, characters);
    }

}
