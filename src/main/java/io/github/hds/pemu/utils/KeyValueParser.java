package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Scanner;

public class KeyValueParser {
    private static final Token STRING = new Token("\"");
    private static final Token CHARACTER = new Token("'");
    private static final Token ESCAPE_CHAR = new Token("\\", true);
    private static final Token ASSIGN = new Token("=");
    private static final Token WHITESPACE = new Token("\\s");
    private static final Token COMMENT = new Token("#");

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

    private static @Nullable Number parseNumber(@NotNull Tokenizer tokenizer) {
        String number = tokenizer.peekNext(WHITESPACE);
        if (number == null) return null;

        try {
            return StringUtils.parseLong(number);
        } catch (Exception ignored) { }

        try {
            return Double.parseDouble(number);
        } catch (Exception ignored) { }

        return null;
    }

    public static @NotNull KeyValueData parseKeyValuePairs(@NotNull Readable readable) {
        Scanner scanner = new Scanner(readable);

        HashMap<String, Object> entries = new HashMap<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            Tokenizer tokenizer = new Tokenizer(line, true, STRING, CHARACTER, ESCAPE_CHAR, ASSIGN, WHITESPACE, COMMENT);
            tokenizer.removeEmpties();

            if (COMMENT.equals(tokenizer.peekNext(WHITESPACE))) continue;

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

            Number number = parseNumber(tokenizer);
            if (number != null) {
                entries.put(key, number);
                continue;
            }

            String bool = tokenizer.peekNext(WHITESPACE);
            if (bool == null) continue;
            if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("false")) {
                entries.put(key, bool.equalsIgnoreCase("true"));
            }
        }

        return new KeyValueData(entries);
    }

}
