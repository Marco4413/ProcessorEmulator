package io.github.hds.pemu.tokenizer.keyvalue;

import io.github.hds.pemu.tokenizer.Token;
import io.github.hds.pemu.tokenizer.TokenDefinition;
import io.github.hds.pemu.tokenizer.Tokenizer;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Scanner;

public final class KeyValueParser {
    protected static final char ESCAPE_CHARACTER = '\\';
    protected static final char STRING_QUOTE = '"';
    protected static final char CHAR_QUOTE   = '\'';
    private static final TokenDefinition STRING     = new TokenDefinition("String", "\"((?:[^\\\\\"]|\\\\.)*)\"");
    private static final TokenDefinition CHARACTER  = new TokenDefinition("Character", "'(\\\\.|[^\\\\])'");
    private static final TokenDefinition COMMENT    = new TokenDefinition("Comment", "#[^\\v]*\\v?");
    private static final TokenDefinition FLOAT      = new TokenDefinition("Float", "[+\\-]?[0-9]+(?:\\.[0-9]+)*");
    private static final TokenDefinition INTEGER    = new TokenDefinition("Integer", "[+\\-]?[0-9]+");
    private static final TokenDefinition BOOLEAN    = new TokenDefinition("Boolean", "true|false");
    private static final TokenDefinition ASSIGNMENT = new TokenDefinition("Assignment", "=", true);
    private static final TokenDefinition WHITESPACE = new TokenDefinition("Whitespace", "\\s+");

    private static final TokenDefinition[] ALL_DEFINITIONS = new TokenDefinition[] {
            STRING, CHARACTER, COMMENT, FLOAT, INTEGER, BOOLEAN, ASSIGNMENT, WHITESPACE
    };

    private static @Nullable String parseString(@Nullable Token token) {
        if (!STRING.isDefinitionOf(token))
            return null;

        assert token != null;
        assert token.getGroups().length > 0;

        String strContent = token.getGroups()[0];
        StringBuilder strBuilder = new StringBuilder();

        boolean isEscaping = false;
        for (int i = 0; i < strContent.length(); i++) {
            char currentChar = strContent.charAt(i);
            if (isEscaping) {
                strBuilder.append(
                        StringUtils.SpecialCharacters.MAP.getOrDefault(currentChar, currentChar)
                );
                isEscaping = false;
            } else if (currentChar == ESCAPE_CHARACTER)
                isEscaping = true;
            else strBuilder.append(strContent.charAt(i));
        }

        return strBuilder.toString();
    }

    private static @Nullable Character parseCharacter(@Nullable Token token) {
        if (!CHARACTER.isDefinitionOf(token))
            return null;

        assert token != null;
        assert token.getGroups().length > 0;

        String charContent = token.getGroups()[0];

        char character;
        if (charContent.charAt(0) == ESCAPE_CHARACTER) {
            char escapedChar = charContent.charAt(1);
            character = StringUtils.SpecialCharacters.MAP.getOrDefault(escapedChar, escapedChar);
        } else character = charContent.charAt(0);
        return character;
    }

    private static @Nullable Number parseNumber(@Nullable Token token) {
        if (INTEGER.isDefinitionOf(token)) {
            assert token != null;
            try {
                return StringUtils.parseLong(token.getMatch());
            } catch (Exception ignored) { }

        } else if (FLOAT.isDefinitionOf(token)) {
            assert token != null;
            try {
                return Double.parseDouble(token.getMatch());
            } catch (Exception ignored) { }
        }

        return null;
    }

    private static @Nullable Boolean parseBoolean(@Nullable Token token) {
        if (!BOOLEAN.isDefinitionOf(token))
            return null;

        assert token != null;
        return token.getMatch().equals("true");
    }

    public static @NotNull KeyValueData parseKeyValuePairs(@NotNull Readable readable) {
        Scanner scanner = new Scanner(readable);

        HashMap<String, Object> entries = new HashMap<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Tokenizer tokenizer = new Tokenizer(line, ALL_DEFINITIONS);
            tokenizer.removeTokensByDefinition(WHITESPACE);

            Token currentToken = tokenizer.goForward();
            if (COMMENT.isDefinitionOf(currentToken)) continue;

            String key = parseString(currentToken);
            if (key == null) continue;

            if (!ASSIGNMENT.isDefinitionOf(tokenizer.goForward())) continue;

            currentToken = tokenizer.goForward();

            String str = parseString(currentToken);
            if (str != null) {
                entries.put(key, str);
                continue;
            }

            Character character = parseCharacter(currentToken);
            if (character != null) {
                entries.put(key, character);
                continue;
            }

            Number number = parseNumber(currentToken);
            if (number != null) {
                entries.put(key, number);
                continue;
            }

            Boolean bool = parseBoolean(currentToken);
            if (bool == null) continue;
            entries.put(key, bool);
        }

        return new KeyValueData(entries);
    }

}
