package io.github.marco4413.pemu.console;

import io.github.marco4413.pemu.utils.IPrintable;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.Writer;

public interface IConsole extends IPrintable {
    @NotNull Writer toWriter();
    @NotNull PrintStream toPrintStream();
}
