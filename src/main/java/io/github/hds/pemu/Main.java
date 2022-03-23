package io.github.hds.pemu;

import io.github.hds.pemu.application.Application;
import io.github.hds.pemu.application.gui.ApplicationGUI;
import io.github.hds.pemu.arguments.*;
import io.github.hds.pemu.console.Console;
import io.github.hds.pemu.localization.TranslationManager;
import io.github.hds.pemu.math.MathUtils;
import io.github.hds.pemu.plugins.DefaultPlugin;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.plugins.PluginManager;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.StringUtils;

import javax.swing.*;
import java.io.File;

public final class Main {

    public static void main(String[] args) {
        Command argParser = new Command(Application.APP_NAME, new Command[] {
                new Command("help"),
                new Command("version"),
                new Command("run"),
                new Command("verify"),
                new Command("obfuscate")
        }, new BaseOption[] {
                new ConstrainedIntegerOption("bits", new String[] { "--bits", "-b" },
                        n -> n == null ? ProcessorConfig.DEFAULT_BITS : MathUtils.constrain(n, ProcessorConfig.MIN_BITS, ProcessorConfig.MAX_BITS)
                ),
                new ConstrainedIntegerOption("memory-size", new String[] { "--memory-size", "-ms" },
                        n -> n == null ? ProcessorConfig.DEFAULT_MEMORY_SIZE : MathUtils.constrain(n, ProcessorConfig.MIN_MEMORY_SIZE, ProcessorConfig.MAX_MEMORY_SIZE)
                ),
                new ConstrainedIntegerOption("clock-frequency", new String[] { "--clock-frequency", "-cf" },
                        n -> n == null ? ProcessorConfig.DEFAULT_FREQUENCY : MathUtils.constrain(n, ProcessorConfig.MIN_FREQUENCY, ProcessorConfig.MAX_FREQUENCY)
                ),
                new StringOption("plugin", new String[] { "--plugin", "-pl" }, DefaultPlugin.getInstance().getID()),
                new StringOption("language", new String[] { "--language", "-lang" }, "en-us"),
                new StringOption("program", new String[] { "--program", "-p" }, "."),
                new FlagOption("command-line", new String[] { "--command-line", "-cl" }),
                new FlagOption("no-config-auto-save", new String[] { "--no-config-auto-save", "-ncas" })
        }).parse(args);

        if (argParser.getCommandByName("help").isSet()) {
            System.out.println(argParser.getUsage());
            return;
        }

        if (argParser.getCommandByName("version").isSet()) {
            System.out.println(StringUtils.format("{0} version \"{1}\"", Application.APP_NAME, Application.APP_VERSION));
            return;
        }

        boolean isCommandLine = argParser.getOptionByName("command-line").isSet();

        // If the user wants the program to run as a console app
        if (isCommandLine) {
            Console.usePrintStream(System.out);
        }

        ConfigManager.setDefaultOnLoadError(true);

        // Queuing DefaultPlugin for register
        PluginManager.queueForRegister(DefaultPlugin.getInstance());

        Application app = Application.getInstance();

        // Initializing GUI if necessary
        if (!isCommandLine) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            ApplicationGUI.getInstance().FRAME.setVisible(true);
            TranslationManager.setCurrentTranslation(TranslationManager.getCurrentTranslation().getShortName());
        }

        // Console Arguments override config settings
        // Setting App's ProcessorConfig based on the specified arguments
        ProcessorConfig processorConfig = app.getProcessorConfig();
        if (argParser.getOptionByName("bits").isSet())
            processorConfig.setBits((int) argParser.getOptionByName("bits").getValue());
        if (argParser.getOptionByName("memory-size").isSet())
            processorConfig.setMemorySize((int) argParser.getOptionByName("memory-size").getValue());
        if (argParser.getOptionByName("clock-frequency").isSet())
            processorConfig.setClockFrequency((int) argParser.getOptionByName("clock-frequency").getValue());

        if (argParser.getOptionByName("language").isSet()) {
            TranslationManager.setCurrentTranslation(
                    (String) argParser.getOptionByName("language").getValue()
            );
        }

        if (argParser.getOptionByName("plugin").isSet()) {
            app.loadPlugin(PluginManager.getPlugin(
                    (String) argParser.getOptionByName("plugin").getValue()
            ));
        }

        app.setCurrentProgram(new File((String) argParser.getOptionByName("program").getValue()));

        // Setting app's flags to prevent setVisible to change the app's visibility
        //  and making it close on Processor Stop if on command line
        app.setFlags(
                  (argParser.getOptionByName("no-config-auto-save").isSet() ? Application.DISABLE_CONFIG_AUTO_SAVE : Application.NONE)
                | (isCommandLine ? Application.CLOSE_ON_PROCESSOR_STOP : Application.NONE)
        );

        app.run();

        boolean closeApplication = isCommandLine;
        if (argParser.getCommandByName("run").isSet()) {
            boolean successfulRun = app.runProcessor();
            // SuccessfulRun is true if no error was encountered and Processor was run
            // If the processor runs the application should close automatically
            closeApplication = closeApplication && !successfulRun;
        } else if (argParser.getCommandByName("verify").isSet()) {
            app.verifyProgram();
        } else if (argParser.getCommandByName("obfuscate").isSet()) {
            app.obfuscateProgram();
        }

        if (closeApplication) app.close();
    }

}

