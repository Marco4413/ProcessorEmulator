package io.github.marco4413.pemu.arguments;

import org.jetbrains.annotations.NotNull;

public final class StringOption extends BaseOption<String> {
    private final String DEFAULT_VALUE;
    private String value;

    public StringOption(@NotNull String name, @NotNull String defaultValue) {
        this(name, new String[] { name }, defaultValue);
    }

    public StringOption(@NotNull String name, @NotNull String[] aliases, @NotNull String defaultValue) {
        super(name, aliases);
        DEFAULT_VALUE = defaultValue;
        value = DEFAULT_VALUE;
    }

    @Override
    public int parseValue(@NotNull String[] args) {
        this.set();
        value = DEFAULT_VALUE;
        if (args.length <= 0) return 0;

        value = args[0];
        return 1;
    }

    @Override
    public @NotNull String getValue() {
        return value;
    }

    @Override
    public @NotNull String getDefinition() {
        return formatDefinition(this, 1, "String");
    }
}
