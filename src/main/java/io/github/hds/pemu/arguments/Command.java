package io.github.hds.pemu.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public final class Command extends BaseEntity {
    private final Command[] SUBCOMMANDS;
    private final BaseOption[] OPTIONS;
    private final ArrayList<String> POSITIONAL_ARGUMENTS;

    public Command(@NotNull String name) {
        this(name, new BaseOption[0]);
    }

    public Command(@NotNull String name, @NotNull BaseOption[] options) {
        this(name, new String[] { name }, options);
    }

    public Command(@NotNull String name, @NotNull Command[] subcommands, @NotNull BaseOption[] options) {
        this(name, new String[] { name }, subcommands, options);
    }

    public Command(@NotNull String name, @NotNull String[] aliases, @NotNull BaseOption[] options) {
        this(name, aliases, new Command[0], options);
    }

    public Command(@NotNull String name, @NotNull String[] aliases, @NotNull Command[] subcommands, @NotNull BaseOption[] options) {
        super(name, aliases);
        this.SUBCOMMANDS = subcommands;
        this.OPTIONS = options;
        this.POSITIONAL_ARGUMENTS = new ArrayList<>();
    }

    public @NotNull Command getCommandByName(@NotNull String name) {
        for (Command subcommand : SUBCOMMANDS)
            if (subcommand.getName().equals(name)) return subcommand;
        throw new NullPointerException("The Specified Command is Null.");
    }

    public @Nullable Command getCommandByAlias(@NotNull String alias) {
        for (Command subcommand : SUBCOMMANDS)
            if (subcommand.isAlias(alias)) return subcommand;
        return null;
    }

    public @NotNull BaseOption getOptionByName(@NotNull String name) {
        for (BaseOption option : OPTIONS)
            if (option.getName().equals(name)) return option;
        throw new NullPointerException("The Specified Option is Null.");
    }

    public @Nullable Command getOptionByAlias(@NotNull String alias) {
        for (Command subcommand : SUBCOMMANDS)
            if (subcommand.isAlias(alias)) return subcommand;
        return null;
    }

    public @NotNull String[] getPositionalArguments() {
        return POSITIONAL_ARGUMENTS.toArray(new String[0]);
    }

    public @NotNull Command parse(@NotNull String[] args) {
        this.set();
        for (int i = 0; i < args.length; i++) {
            boolean argParsed = false;
            if (SUBCOMMANDS.length > 0 || OPTIONS.length > 0) {
                String[] subArguments = Arrays.copyOfRange(args, i + 1, args.length);
                for (Command subcommand : SUBCOMMANDS) {
                    if (subcommand.isAlias(args[i])) {
                        subcommand.parse(subArguments);
                        return this;
                    }
                }

                for (BaseOption option : OPTIONS) {
                    if (option.isAlias(args[i])) {
                        argParsed = true;
                        i += option.parseValue(subArguments);
                        break;
                    }
                }
            }

            if (!argParsed)
                POSITIONAL_ARGUMENTS.add(args[i]);
        }
        return this;
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("^.*\\n*$", Pattern.MULTILINE);
    public @NotNull String getUsage() {
        if (OPTIONS.length == 0 && SUBCOMMANDS.length == 0) {
            return NAME + "\n";
        }

        StringBuilder usage = new StringBuilder(NAME);
        if (OPTIONS.length > 0)
            usage.append(" [OPTIONS]");
        if (SUBCOMMANDS.length > 0)
            usage.append(" [COMMAND]");
        usage.append(":\n");

        if (OPTIONS.length > 0) {
            usage.append("> Options:\n");
            for (BaseOption option : OPTIONS)
                usage.append("\t").append(option.getDefinition()).append('\n');
        }

        if (SUBCOMMANDS.length > 0) {
            usage.append("> Commands:\n");
            for (Command command : SUBCOMMANDS)
                // This matches all lines and puts a tab character in front of them
                usage.append(LINE_PATTERN.matcher(command.getUsage()).replaceAll("\t$0"));
        }

        return usage.toString();
    }
}
