package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class BaseEntity {
    protected final String NAME;
    protected final String[] ALIASES;
    private boolean isSet;

    public BaseEntity(@NotNull String name) {
        this(name, new String[] { name });
    }

    public BaseEntity(@NotNull String name, @NotNull String[] aliases) {
        this.NAME = name;
        this.ALIASES = aliases;
        this.isSet = false;
    }

    protected void set() {
        this.isSet = true;
    }

    protected void unset() {
        this.isSet = false;
    }

    public boolean isSet() {
        return this.isSet;
    }

    public @NotNull String getName() { return NAME; }

    public @NotNull String[] getAliases() {
        return Arrays.copyOf(ALIASES, ALIASES.length);
    }

    public boolean isAlias(@NotNull String str) {
        for (int i = 0; i < ALIASES.length; i++)
            if (ALIASES[i].equals(str)) return true;
        return false;
    }
}
