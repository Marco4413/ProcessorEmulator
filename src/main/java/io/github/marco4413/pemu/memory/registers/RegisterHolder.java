package io.github.marco4413.pemu.memory.registers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public final class RegisterHolder<T extends IRegister> {

    private final HashMap<String, T> REGISTERS;

    public RegisterHolder() {
        REGISTERS = new HashMap<>();
    }

    public RegisterHolder(T register) {
        this();
        addRegister(register);
    }

    @SafeVarargs
    public RegisterHolder(T... registers) {
        this();
        addRegisters(registers);
    }

    public int getCount() {
        return REGISTERS.size();
    }

    public @NotNull IRegister[] toArray() {
        return REGISTERS.values().toArray(new IRegister[0]);
    }

    public @NotNull T[] toArray(T[] array) {
        return REGISTERS.values().toArray(array);
    }

    public @NotNull RegisterHolder<T> addRegister(T register) {
        String shortName = register.getShortName();
        if (REGISTERS.containsKey(shortName))
            throw new IllegalArgumentException("Registers can't have duplicate short names (" + shortName + ")!");
        REGISTERS.put(register.getShortName(), register);
        return this;
    }

    @SafeVarargs
    public final @NotNull RegisterHolder<T> addRegisters(T... registers) {
        for (T register : registers) addRegister(register);
        return this;
    }

    public @Nullable T removeRegister(@NotNull String shortName) {
        return REGISTERS.remove(shortName);
    }

    public @Nullable T getRegister(@NotNull String shortName) {
        return REGISTERS.get(shortName);
    }

    public @NotNull RegisterHolder<T> forEach(@NotNull BiConsumer<String, T> action) {
        REGISTERS.forEach(action);
        return this;
    }

}
