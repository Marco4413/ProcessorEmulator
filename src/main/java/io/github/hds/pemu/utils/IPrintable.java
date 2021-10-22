package io.github.hds.pemu.utils;

import io.github.hds.pemu.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPrintable {
    void print(@Nullable String string);
    void print(boolean bool);
    void print(char character);
    void print(int number);
    void print(long number);
    void print(float number);
    void print(double number);
    void print(@Nullable Object object);

    default void print(@Nullable String... strings) { for (String string : strings) print(string); }
    default void print(boolean... booleans) { for (boolean bool : booleans) print(bool); }
    default void print(char... characters) { for (char character : characters) print(character); }
    default void print(int... numbers) { for (int number : numbers) print(number); }
    default void print(long... numbers) { for (long number : numbers) print(number); }
    default void print(float... numbers) { for (float number : numbers) print(number); }
    default void print(double... numbers) { for (double number : numbers) print(number); }
    default void print(@Nullable Object... objects) { for (Object object : objects) print(object); }

    default void println() {
        print('\n');
    }
    default void println(@Nullable String... strings) { print(strings); println(); }
    default void println(boolean... booleans) { print(booleans); println(); }
    default void println(char... characters) { print(characters); println(); }
    default void println(int... numbers) { print(numbers); println(); }
    default void println(long... numbers) { print(numbers); println(); }
    default void println(float... numbers) { print(numbers); println(); }
    default void println(double... numbers) { print(numbers); println(); }
    default void println(@Nullable Object... objects) { print(objects); println(); }

    default void printStackTrace(@NotNull Throwable err) {
        println(StringUtils.stackTraceAsString(err));
    }

    default void printStackTrace(@NotNull Throwable err, boolean printBacktraceForKnownExceptions) {
        if (printBacktraceForKnownExceptions)
            printStackTrace(err);
        else {
            boolean isKnown = err.getClass().getPackage().getName().startsWith(Main.class.getPackage().getName());
            if (isKnown) println(err.getMessage());
            else printStackTrace(err);
        }
    }
}
