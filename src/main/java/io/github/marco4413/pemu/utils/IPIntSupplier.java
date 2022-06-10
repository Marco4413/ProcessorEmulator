package io.github.marco4413.pemu.utils;

@FunctionalInterface
public interface IPIntSupplier {
    int get(Object data);
    default int get() { return get(null); }
}
