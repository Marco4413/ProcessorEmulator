package io.github.marco4413.pemu.math.parser;

import io.github.marco4413.pemu.tokenizer.ITokenDefinition;
import io.github.marco4413.pemu.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class ParserContext {
    public final Tokenizer tokenizer;
    public final ITokenDefinition VAR_DEFINITION;
    public final IVarProcessor VAR_PROCESSOR;
    public final File SOURCE_FILE;
    public final int SOURCE_LINE;
    public final int SOURCE_LINE_CHAR;

    protected ParserContext(
            @NotNull Tokenizer tokenizer,
            @NotNull ITokenDefinition varDef, @NotNull IVarProcessor varProcessor,
            @Nullable File sourceFile, int sourceLine, int sourceLineChar
    ) {
        this.tokenizer = tokenizer;
        this.VAR_DEFINITION = varDef;
        this.VAR_PROCESSOR = varProcessor;
        this.SOURCE_FILE = sourceFile;
        this.SOURCE_LINE = sourceLine;
        this.SOURCE_LINE_CHAR = sourceLineChar;
    }

    public int getCurrentLine() {
        return tokenizer.getCurrentLine() + SOURCE_LINE - 1;
    }

    public int getCurrentLineChar() {
        if (tokenizer.getCurrentLine() > 1)
            return tokenizer.getCurrentLineChar();
        return tokenizer.getCurrentLineChar() + SOURCE_LINE_CHAR - 1;
    }
}
