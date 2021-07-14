package io.github.hds.pemu.app;

import io.github.hds.pemu.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public final class Console {

    private static final @NotNull ConsoleComponent PROGRAM_COMPONENT = new ConsoleComponent();
    private static final @NotNull ConsoleComponent DEBUG_COMPONENT = new ConsoleComponent();

    public static volatile @NotNull IPrintable ProgramOutput = PROGRAM_COMPONENT;
    public static volatile @NotNull IPrintable Debug = DEBUG_COMPONENT;

    public static void usePrintStream(@Nullable PrintStream stream) {
        if (stream == null) {
            ProgramOutput = PROGRAM_COMPONENT;
            Debug = DEBUG_COMPONENT;
        } else {
            IPrintable printableStream = new PrintablePrintStream(stream);
            ProgramOutput = printableStream;
            Debug = printableStream;
        }
    }

    public static @NotNull ConsoleComponent getProgramComponent() {
        return PROGRAM_COMPONENT;
    }

    public static @NotNull ConsoleComponent getDebugComponent() {
        return DEBUG_COMPONENT;
    }

}
