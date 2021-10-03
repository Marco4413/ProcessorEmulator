package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class TokenDefinition extends AbstractTokenDefinition {

    private final Pattern PATTERN;

    public TokenDefinition(@NotNull String name, @NotNull String regex) {
        this(name, regex, false);
    }

    public TokenDefinition(@NotNull String name, @NotNull String regex, boolean literal) {
        this(name, regex, literal, false);
    }

    public TokenDefinition(@NotNull String name, @NotNull String regex, boolean literal, boolean ignoreCase) {
        super(name);

        int flags = Pattern.DOTALL;
        if (literal) flags |= Pattern.LITERAL;
        if (ignoreCase) flags |= Pattern.CASE_INSENSITIVE;

        PATTERN = Pattern.compile(
                regex, flags
        );
    }

    @Override
    public @NotNull Pattern getPattern() {
        return PATTERN;
    }

}
