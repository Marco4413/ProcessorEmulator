package io.github.marco4413.pemu.compiler.parser;

import io.github.marco4413.pemu.compiler.CompilerVars;
import io.github.marco4413.pemu.processor.IProcessor;
import io.github.marco4413.pemu.tokenizer.Tokenizer;
import io.github.marco4413.pemu.utils.IPIntSupplier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ParserContext {
    public final IProcessor processor;
    public final Tokenizer tokenizer;

    private @NotNull File currentFile;
    private final CompilerVars COMPILER_VARS;
    private final ArrayList<INode> NODES = new ArrayList<>();

    protected ParserContext(@NotNull IProcessor processor, @NotNull File file, @NotNull Tokenizer tokenizer, @NotNull CompilerVars startingCVars) {
        this.processor = processor;
        this.currentFile = file;
        this.tokenizer = tokenizer;
        COMPILER_VARS = startingCVars;
    }

    public void setCurrentFile(@NotNull File file) {
        currentFile = file;
    }

    public @NotNull File getCurrentFile() {
        return currentFile;
    }

    public boolean hasCompilerVar(@NotNull String name) {
        return COMPILER_VARS.containsKey(name);
    }

    public IPIntSupplier getCompilerVar(@NotNull String name) {
        return COMPILER_VARS.get(name);
    }

    public void putCompilerVar(@NotNull String name, IPIntSupplier value) {
        COMPILER_VARS.put(name, value);
    }

    public int getCurrentLine() {
        return tokenizer.getCurrentLine();
    }

    public int getCurrentLineChar() {
        return tokenizer.getCurrentLineChar();
    }

    public void addNode(@NotNull INode node) {
        NODES.add(node);
    }

    public void addNodes(@NotNull List<INode> nodes) {
        NODES.addAll(nodes);
    }

    public @NotNull List<INode> getNodes() {
        return NODES;
    }
}
