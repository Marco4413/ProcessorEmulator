package io.github.marco4413.pemu.console;

import org.jetbrains.annotations.NotNull;

import java.io.Writer;

public final class ConsoleWriter extends Writer {

    private final IConsole WRAPPED_CONSOLE;
    private final StringBuffer BUFFER = new StringBuffer();

    public ConsoleWriter(@NotNull IConsole console) {
        WRAPPED_CONSOLE = console;
    }

    @Override
    public synchronized void write(char[] cbuf, int off, int len) {
        BUFFER.append(cbuf, off, len);
    }

    @Override
    public synchronized void flush() {
        WRAPPED_CONSOLE.print(BUFFER.toString());
        BUFFER.setLength(0);
    }

    @Override
    public void close() { }
}
