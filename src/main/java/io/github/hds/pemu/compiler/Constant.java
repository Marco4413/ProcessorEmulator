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

    public static @NotNull String formatReferences(@NotNull String prefix, @NotNull ArrayList<String> references) {
        if (references.size() == 0) return "";
        return prefix + String.join(Compiler.Tokens.WHITESPACE.getCharacter() + prefix, references);
    }

    public boolean hasReference() {
        return reference != null;
    }

    public @Nullable Constant getReference() {
        return reference;
    }

    public @NotNull Constant setReference(@Nullable Constant reference) {
        this.reference = reference;
        return this;
    }

    public boolean isCircularReference(@Nullable ArrayList<String> references) {
        Constant currentReference = this;
        if (references != null) references.add(currentReference.getName());

        do {
            currentReference = currentReference.getReference();
            if (currentReference == null) return false;
            if (references != null) references.add(currentReference.getName());
        } while (currentReference != this);

        return true;
    }

    public @NotNull Constant setValue(int value) {
        this.value = value;
        return setReference(null);
    }

    public @NotNull String getName() {
        return NAME;
    }

    public int getValue() {
        return getValue(null);
    }

    public int getValue(@Nullable ArrayList<String> references) {
        Constant currentReference = this;
        if (references != null) references.add(currentReference.getName());

        do {
            if (!currentReference.hasReference()) return currentReference.value;
            currentReference = currentReference.getReference();
            assert currentReference != null; // It should never be null
            if (references != null) references.add(currentReference.getName());
        } while (currentReference != this);

        throw new IllegalStateException("Circular Constant Reference");
    }

    public @NotNull Constant addInstance(int address) {
        INSTANCES.add(address);
        return this;
    }

    public @NotNull Integer[] getInstances() {
        return INSTANCES.toArray(new Integer[0]);
    }

}
