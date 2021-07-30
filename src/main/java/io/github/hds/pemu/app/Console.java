package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public final class Console {

    private static final @NotNull ConsoleComponent PROGRAM_COMPONENT = new ConsoleComponent();
    private static final @NotNull ConsoleComponent DEBUG_COMPONENT = new ConsoleComponent();

    public static volatile @NotNull IConsole ProgramOutput = PROGRAM_COMPONENT;
    public static volatile @NotNull IConsole Debug = DEBUG_COMPONENT;

    public static synchronized void usePrintStream(@Nullable PrintStream stream) {
        if (stream == null) {
            ProgramOutput = PROGRAM_COMPONENT;
            Debug = DEBUG_COMPONENT;
        } else {
            IConsole consoleStream = new ConsolePrintStream(stream);
            ProgramOutput = consoleStream;
            Debug = consoleStream;
        }
    }

    public static @NotNull ConsoleComponent getProgramComponent() {
        return PROGRAM_COMPONENT;
    }

    public static @NotNull ConsoleComponent getDebugComponent() {
        return DEBUG_COMPONENT;
    }

}
