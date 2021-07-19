package io.github.hds.pemu.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class Constant {

    private final @NotNull String NAME;
    private int value;
    private final ArrayList<Integer> INSTANCES;
    private Constant reference = null;

    public Constant(@NotNull String name) {
        this(name, 0);
    }

    public Constant(@NotNull String name, int initialValue) {
        NAME = name;
        value = initialValue;
        INSTANCES = new ArrayList<>();
    }

    public boolean isReference() {
        return reference != null;
    }

    public @Nullable Constant getReference() {
        return reference;
    }

    public @NotNull Constant setReference(@Nullable Constant reference) {
        this.reference = reference;
        return this;
    }

    public @NotNull Constant setValue(int value) {
        this.value = value;
        return setReference(null);
    }

    public @NotNull String getName() {
        return NAME;
    }

    public int getValue() {
        return getValue(null, null);
    }

    public int getValue(@NotNull ArrayList<String> references) {
        return getValue(null, references);
    }

    private int getValue(@Nullable Constant initialConstant, @Nullable ArrayList<String> references) {
        if (references != null) references.add(NAME);
        if (initialConstant == this)
            throw new IllegalArgumentException("Circular Constant Reference");
        return isReference() ? reference.getValue(initialConstant == null ? this : initialConstant, references) : value;
    }

    public @NotNull Constant addInstance(int address) {
        INSTANCES.add(address);
        return this;
    }

    public @NotNull Integer[] getInstances() {
        return INSTANCES.toArray(new Integer[0]);
    }

}
