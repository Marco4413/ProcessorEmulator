package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class Token {

    private final char CHARACTER;
    private final @NotNull String PATTERN;
    private final boolean IS_QUOTED;

    public Token(char character) {
        this(character, false);
    }

    public Token(char character, boolean quoteCharacter) {
        this(character, String.valueOf(character), quoteCharacter);
    }

    public Token(char character, @NotNull String pattern, boolean quotePattern) {
        CHARACTER = character;
        PATTERN = pattern;
        IS_QUOTED = quotePattern;
    }

    public boolean isQuoted() {
        return IS_QUOTED;
    }

    public char getCharacter() {
        return CHARACTER;
    }

    public @NotNull String getPattern() {
        return getPattern(IS_QUOTED);
    }

    public @NotNull String getPattern(boolean quoted) {
        return quoted ? Pattern.quote(PATTERN) : PATTERN;
    }

    public boolean matches(@Nullable String str) {
        if (str == null) return false;
        return str.matches("[" + getPattern() + "]+");
    }

}
