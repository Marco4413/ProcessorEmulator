package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public interface ITokenDefinition {

    @NotNull Pattern getPattern();
    @NotNull String getName();

    default boolean isDefinitionOf(@Nullable Token token) {
        return token != null && this.equals(token.getDefinition());
    }

}
