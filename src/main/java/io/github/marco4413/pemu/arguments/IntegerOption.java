package io.github.marco4413.pemu.arguments;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public final class IntegerOption extends BaseOption<Integer> {
    private final int DEFAULT_VALUE;
    private int value;

    public IntegerOption(@NotNull String name, int defaultValue) {
        this(name, new String[] { name }, defaultValue);
    }

    public IntegerOption(@NotNull String name, @NotNull String[] aliases, int defaultValue) {
        super(name, aliases);
        DEFAULT_VALUE = defaultValue;
        value = defaultValue;
    }

    @Override
    public int parseValue(@NotNull String[] args) {
        this.set();
        value = DEFAULT_VALUE;
        if (args.length <= 0) return 0;

        try {
            value = StringUtils.parseInt(args[0]);
            return 1;
        } catch (Exception err) {
            return 0;
        }
    }

    @Override
    public @NotNull Integer getValue() {
        return value;
    }

    @Override
    public @NotNull String getDefinition() {
        return formatDefinition(this, 1, "Integer");
    }
}
