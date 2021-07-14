package io.github.hds.pemu.utils;

import io.github.hds.pemu.Main;
import org.jetbrains.annotations.NotNull;

public interface IPrintable {

    void print(String string);

    default void print(String... strings) {
        for (String string : strings) print(string);
    }

    void print(char character);

    default void print(char... characters) {
        for (char character : characters) print(character);
    }

    void print(int integer);

    default void print(int... integers) {
        for (int integer : integers) print(integer);
    }

    default void println() {
        print('\n');
    }

    default void println(String... strings) {
        print(strings);
        println();
    }

    default void println(char... characters) {
        print(characters);
        println();
    }

    default void println(int... integers) {
        print(integers);
        println();
    }

    void printStackTrace(@NotNull Exception err);

    default void printStackTrace(@NotNull Exception err, boolean printBacktraceForKnownExceptions) {
        if (printBacktraceForKnownExceptions)
            printStackTrace(err);
        else {
            boolean isKnown = err.getClass().getPackage().getName().startsWith(Main.class.getPackage().getName());
            if (isKnown) println(err.getMessage());
            else printStackTrace(err);
        }
    }
}
