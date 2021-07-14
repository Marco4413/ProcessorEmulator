package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

public final class PrintablePrintStream implements IPrintable {

    public final PrintStream STREAM;

    public PrintablePrintStream(@NotNull PrintStream stream) {
        STREAM = stream;
    }

    @Override
    public synchronized void print(String string) {
        STREAM.append(string);
    }

    @Override
    public synchronized void print(char character) {
        STREAM.append(String.valueOf(character));
    }

    @Override
    public synchronized void print(int integer) {
        STREAM.append(String.valueOf(integer));
    }

    @Override
    public synchronized void printStackTrace(@NotNull Exception err) {
        err.printStackTrace(STREAM);
    }
}
