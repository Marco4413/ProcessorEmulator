package io.github.marco4413.pemu.arguments;

import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public abstract class BaseOption<T> extends BaseEntity {
    public BaseOption(@NotNull String name) {
        super(name);
    }

    public BaseOption(@NotNull String name, @NotNull String[] aliases) {
        super(name, aliases);
    }

    public abstract int parseValue(@NotNull String[] args);

    public abstract T getValue();

    public abstract @NotNull String getDefinition();

    public static @NotNull String formatDefinition(@NotNull BaseOption<?> option, int argCount, @NotNull String optionType) {
        return StringUtils.format(
                "{0}: {1} = ({2}) -> {3}",
                option.ALIASES.length > 0 ? option.ALIASES[0] : option.NAME,
                option.getClass().getSimpleName(), argCount, optionType
        );
    }
}
