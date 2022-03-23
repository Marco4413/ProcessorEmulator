package io.github.hds.pemu.arguments;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class ConstrainedIntegerOption extends BaseOption<Integer> {
    private final Function<@Nullable Integer, @NotNull Integer> CONSTRAINT;
    private int value;

    public ConstrainedIntegerOption(@NotNull String name, @NotNull Function<Integer, Integer> constraint) {
        this(name, new String[] { name }, constraint);
    }

    public ConstrainedIntegerOption(@NotNull String name, @NotNull String[] aliases, @NotNull Function<@Nullable Integer, @NotNull Integer> constraint) {
        super(name, aliases);
        CONSTRAINT = constraint;
        value = CONSTRAINT.apply(null);
    }

    @Override
    public int parseValue(@NotNull String[] args) {
        this.set();
        if (args.length <= 0) {
            CONSTRAINT.apply(null);
            return 0;
        }

        try {
            value = CONSTRAINT.apply(StringUtils.parseInt(args[0]));
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
