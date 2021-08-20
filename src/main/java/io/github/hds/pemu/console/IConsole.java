package io.github.hds.pemu.console;

import io.github.hds.pemu.utils.IPrintable;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.Writer;

public interface IConsole extends IPrintable {
    @NotNull Writer toWriter();
    @NotNull PrintStream toPrintStream();
}
