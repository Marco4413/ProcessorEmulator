package io.github.hds.pemu.compiler.parser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public final class CompilerVarStack {
    private final ArrayList<CompilerVarDeclaration> VAR_STACK;

    protected CompilerVarStack(@NotNull CompilerVarDeclaration rootDeclaration) {
        VAR_STACK = new ArrayList<>(Collections.singleton(rootDeclaration));
    }

    public void push(@NotNull CompilerVarDeclaration varDeclaration) {
        VAR_STACK.add(varDeclaration);
    }

    public @NotNull CompilerVarDeclaration pop() {
        return VAR_STACK.remove(VAR_STACK.size() - 1);
    }

    public boolean isInStack(@NotNull String varName) {
        for (int i = VAR_STACK.size() - 1; i >= 0; i--) {
            if (VAR_STACK.get(i).getName().equals(varName))
                return true;
        }
        return false;
    }
}
