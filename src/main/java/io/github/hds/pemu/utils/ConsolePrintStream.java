package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Objects;

public final class ConsolePrintStream implements IConsole {

    public final PrintStream STREAM;
    public final PrintWriter WRITER;

    public ConsolePrintStream(@NotNull PrintStream stream) {
        STREAM = stream;
        WRITER = new PrintWriter(STREAM);
    }

    @Override
    public synchronized void print(@Nullable String string) {
        STREAM.append(Objects.toString(string));
    }

    @Override
    public synchronized void print(boolean bool) {
        STREAM.append(String.valueOf(bool));
    }

    @Override
    public synchronized void print(char character) {
        STREAM.append(String.valueOf(character));
    }

    @Override
    public synchronized void print(int number) {
        STREAM.append(String.valueOf(number));
    }

    @Override
    public synchronized void print(long number) {
        STREAM.append(String.valueOf(number));
    }

    @Override
    public synchronized void print(float number) {
        STREAM.append(String.valueOf(number));
    }

    @Override
    public synchronized void print(double number) {
        STREAM.append(String.valueOf(number));
    }

    @Override
    public synchronized void print(@Nullable Object object) {
        STREAM.append(Objects.toString(object));
    }

    @Override
    public synchronized void printStackTrace(@NotNull Exception err) {
        err.printStackTrace(STREAM);
    }

    @Override
    public synchronized @NotNull Writer getWriter() {
        return WRITER;
    }

    @Override
    public synchronized @NotNull PrintStream getPrintStream() {
        return STREAM;
    }
}
