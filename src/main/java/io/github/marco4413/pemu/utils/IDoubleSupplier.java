package io.github.marco4413.pemu.utils;

import java.util.function.Supplier;

@FunctionalInterface
public interface IDoubleSupplier extends Supplier<Double> {
    Double get(Object data);
    default Double get() { return get(null); }
}
