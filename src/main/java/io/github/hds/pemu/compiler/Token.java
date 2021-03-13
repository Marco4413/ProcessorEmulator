package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Token {

    public final String REGEX;

    public Token(@NotNull String regex) {
        REGEX = regex;
    }

    public boolean equals(@Nullable String str) {
        if (str == null) return false;
        return str.matches("[" + REGEX + "]+");
    }

}
