package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;

public final class FlagOption extends BaseOption<Boolean> {
    public FlagOption(@NotNull String name) {
        super(name);
    }

    public FlagOption(@NotNull String name, @NotNull String[] aliases) {
        super(name, aliases);
    }

    @Override
    public int parseValue(@NotNull String[] args) {
        this.set();
        return 0;
    }

    @Override
    public @NotNull Boolean getValue() {
        return this.isSet();
    }

    @Override
    public @NotNull String getDefinition() {
        return formatDefinition(this, 0, "Boolean");
    }
}
