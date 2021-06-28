package io.github.hds.pemu.arguments;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public final class ArgumentOptions {

    public static class Int extends ArgumentOption<Integer> {
        public Int(@NotNull String name, @NotNull String shortName, @NotNull Integer defaultValue) {
            super(name, shortName, defaultValue);
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public void parse(@NotNull String[] args) {
            this.value = StringUtils.parseInt(args[0]);
        }
    }

    public static class RangedInt extends RangedArgumentOption<Integer> {

        private static final DecimalFormat FORMAT = new DecimalFormat("0.###E0");
        private static final int FORMAT_THRESHOLD = 10000;

        public RangedInt(@NotNull String name, @Nullable String shortName, @NotNull Integer defaultValue, @NotNull Integer minValue, @NotNull Integer maxValue) {
            super(name, shortName, defaultValue, minValue, maxValue);
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public void parse(@NotNull String[] args) {
            this.value = StringUtils.parseInt(args[0]);
            validate();
        }

        @Override
        public String valueToString() {
            return this.value.getClass().getSimpleName()
                    + "[" + (MIN_VALUE >= FORMAT_THRESHOLD ? FORMAT.format(MIN_VALUE) : MIN_VALUE.toString())
                    + "; " + (MAX_VALUE >= FORMAT_THRESHOLD ? FORMAT.format(MAX_VALUE) : MAX_VALUE.toString()) + "]";
        }
    }

    public static class Str extends ArgumentOption<String> {
        public Str(@NotNull String name, @NotNull String shortName, @NotNull String defaultValue) {
            super(name, shortName, defaultValue);
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public void parse(@NotNull String[] args) {
            this.value = args[0];
        }
    }

    public static class Flag extends ArgumentOption<Boolean> {
        public Flag(@NotNull String name, @NotNull String shortName, @NotNull Boolean defaultValue) {
            super(name, shortName, defaultValue);
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public void parse(@NotNull String[] args) {
            this.value = true;
        }
    }
}
