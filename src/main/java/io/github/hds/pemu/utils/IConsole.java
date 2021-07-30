package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.Writer;

public interface IConsole extends IPrintable {
    @NotNull Writer getWriter();
    @NotNull PrintStream getPrintStream();
}
