package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class Token {

    private final char CHARACTER;
    private final @NotNull String PATTERN;

    public Token(char character) {
        this(character, false);
    }

    public Token(char character, boolean quoteCharacter) {
        this(character, String.valueOf(character), quoteCharacter);
    }

    public Token(char character, @NotNull String pattern, boolean quotePattern) {
        CHARACTER = character;
        // Compiling the pattern so that we're sure it's valid
        PATTERN = Pattern.compile(
                quotePattern ? Pattern.quote(pattern) : pattern
        ).pattern();
    }

    public char getCharacter() {
        return CHARACTER;
    }

    public @NotNull String getPattern() {
        return PATTERN;
    }

    public boolean matches(@Nullable String str) {
        if (str == null) return false;
        return str.matches(PATTERN);
    }

}
