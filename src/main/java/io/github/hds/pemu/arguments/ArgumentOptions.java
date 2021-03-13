package io.github.hds.pemu.arguments;

import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ArgumentOptions {

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
