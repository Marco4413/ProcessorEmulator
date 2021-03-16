package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ArgumentsParser {

    private final HashMap<String, ArgumentOption> options = new HashMap<>();

    public ArgumentsParser() { }

    public String getUsage() {
        StringBuilder usage = new StringBuilder();
        options.forEach((key, option) -> {
            // The format is "OptionName: OptionClassName = (nArguments) -> ValueType"
            usage.append('\t')
                    .append(option.NAME)
                    .append(": ")
                    .append(option.getClass().getSimpleName())
                    .append(" = (")
                    .append(option.getLength())
                    .append(") -> ")
                    .append(option.value.getClass().getSimpleName())
                    .append('\n');
        });
        return usage.toString();
    }

    public void parse(String[] args) {
        if (args == null || args.length <= 0) return;
        options.forEach((key, option) -> {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) continue;

                if (option.matches(args[i])) {
                    String[] optionArgs = new String[option.getLength()];
                    for (int j = 0; j < optionArgs.length; j++) {
                        int argIndex = i + j + 1;
                        boolean isValidArgument = argIndex < args.length && args[argIndex] != null;
                        if (!isValidArgument) throw new IllegalArgumentException(option.NAME + " requires " + option.getLength() + " arguments, " + j + " were provided!");
                        optionArgs[j] = args[argIndex];
                    }

                    try {
                        option.parse(optionArgs);
                    } catch (Exception err) {
                        throw new IllegalArgumentException("Invalid argument for option " + option.NAME);
                    }
                }
            }
        });
    }

    public @NotNull ArgumentsParser defineInt(@NotNull String name, @NotNull String shortName, @NotNull Integer defaultValue) {
        options.put(name, new ArgumentOptions.Int(name, shortName, defaultValue));
        return this;
    }

    public @NotNull ArgumentsParser defineDbl(@NotNull String name, @NotNull String shortName, @NotNull Double defaultValue) {
        options.put(name, new ArgumentOptions.Dbl(name, shortName, defaultValue));
        return this;
    }

    public @NotNull ArgumentsParser defineStr(@NotNull String name, @NotNull String shortName, @NotNull String defaultValue) {
        options.put(name, new ArgumentOptions.Str(name, shortName, defaultValue));
        return this;
    }

    public @NotNull ArgumentsParser defineFlag(@NotNull String name, @NotNull String shortName) {
        options.put(name, new ArgumentOptions.Flag(name, shortName, false));
        return this;
    }

    public @NotNull ArgumentOption getOption(@NotNull String name) {
        if (!options.containsKey(name)) throw new IllegalArgumentException(name + " isn't a valid option!");
        return options.get(name);
    }

}
