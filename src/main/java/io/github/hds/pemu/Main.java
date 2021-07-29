package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.app.Console;
import io.github.hds.pemu.plugins.BasePlugin;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.plugins.PluginManager;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.utils.StringUtils;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class Main {

    public static void main(String[] args) {

        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("--help", "-h")
              .defineFlag("--version", "-ver")
              .defineFlag("--run", "-r")
              .defineFlag("--verify", "-v")
              .defineFlag("--obfuscate", "-o")
              .defineFlag("--command-line", "-cl")
              .defineFlag("--skip-warning", "-sw")
              .defineFlag("--no-config-auto-save", "-ncas")
              .defineRangedInt("--bits", "-b", ProcessorConfig.DEFAULT_BITS, ProcessorConfig.MIN_BITS, ProcessorConfig.MAX_BITS)
              .defineRangedInt("--memory-size", "-ms", ProcessorConfig.DEFAULT_MEMORY_SIZE, ProcessorConfig.MIN_MEMORY_SIZE, ProcessorConfig.MAX_MEMORY_SIZE)
              .defineRangedInt("--clock-frequency", "-cf", ProcessorConfig.DEFAULT_FREQUENCY, ProcessorConfig.MIN_FREQUENCY, ProcessorConfig.MAX_FREQUENCY)
              .defineStr("--program", "-p", "");
        // Parse Arguments
        parser.parse(args);

        // If help flag was specified, print help and return
        boolean printHelp = parser.isSpecified("--help");
        boolean printVersion = parser.isSpecified("--version");
        if (printHelp || printVersion) {
            if (printVersion) System.out.println(StringUtils.format("{0} version \"{1}\"", Application.APP_TITLE, Application.APP_VERSION));
            if (printHelp) System.out.println("PEMU [options]:\n" + parser.getUsage());
            return;
        }

        boolean isCommandLine = parser.isSpecified("--command-line");
        boolean runOnStart = parser.isSpecified("--run");
        boolean verifyOnStart = parser.isSpecified("--verify");
        boolean obfuscateOnStart = parser.isSpecified("--obfuscate");

        int onStartFlagCount = 0;
        for (boolean f : new boolean[] { runOnStart, verifyOnStart, obfuscateOnStart })
            if (f) onStartFlagCount++;

        if (onStartFlagCount > 1) {
            System.err.println("Only one of \"--run\", \"--verify\" and \"--obfuscate\" flags can be set at once");
            return;
        }

        // If the user wants the program to run as a console app
        if (isCommandLine) {
            // Auto run must be specified, because otherwise the program wouldn't run
            if (onStartFlagCount == 0) {
                System.err.println("Either \"--run\", \"--verify\" or \"--obfuscate\" flag must be specified with the \"--command-line\" flag");
                return;
            }

            if (!parser.isSpecified("--skip-warning")) {
                // Making sure that's what the user wants, because there are some compatibility issues with this choice
                //  (Those are the issues that pushed me into making a Swing app)
                System.out.println("Note that not all programs run properly on the console (See: https://github.com/hds536jhmk/ProcessorEmulator/blob/master/DOCUMENTATION.md#running-on-the-command-line)");
                System.out.println("Are you sure you want to continue? (Y|N)");

                // Initializing System.in Scanner and user choice
                Scanner userInput = new Scanner(System.in);
                String userChoice;

                // This filters Yes and No
                Pattern optionFilter = Pattern.compile("(Y(es)?)|(No?)", Pattern.CASE_INSENSITIVE);

                while (true) {
                    // Get the user's choice
                    userChoice = userInput.nextLine();

                    // If it falls within the choice filter, break
                    if (optionFilter.matcher(userChoice).find()) break;

                    // If not then print the valid choices to the user
                    System.out.println("Invalid choice, valid choices are: (Yes, Y | No, N)");
                }

                // If the choice starts with "n" then the user has chosen "No"
                //  so we return from main else we initialize the Console's output
                if (userChoice.toLowerCase().startsWith("n")) return;
            }

            Console.usePrintStream(System.out);
        }

        // Setting System-based look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Trying to get an instance of the app
        ConfigManager.setDefaultOnLoadError(true);
        Application app = Application.getInstance();

        // Setting App's ProcessorConfig based on the specified arguments
        ProcessorConfig processorConfig = app.getProcessorConfig();
        if (parser.isSpecified("--bits"))
            processorConfig.setBits((int) parser.getOption("--bits").getValue());
        if (parser.isSpecified("--memory-size"))
            processorConfig.setMemorySize((int) parser.getOption("--memory-size").getValue());
        if (parser.isSpecified("--clock-frequency"))
            processorConfig.setClockFrequency((int) parser.getOption("--clock-frequency").getValue());

        // Adding base plugin
        app.loadPlugin(
            PluginManager.registerPlugin(new BasePlugin())
        );

        app.setCurrentProgram(new File((String) parser.getOption("--program").getValue()));

        // Setting the app visible only if not on command line
        app.setVisible(!isCommandLine);

        // Setting app's flags to prevent setVisible to change the app's visibility
        //  and making it close on Processor Stop if on command line
        app.setFlags(
                  (isCommandLine ? Application.CLOSE_ON_PROCESSOR_STOP : Application.NONE)
                | Application.PREVENT_VISIBILITY_CHANGE
                | (parser.isSpecified("--no-config-auto-save") ? Application.DISABLE_CONFIG_AUTO_SAVE : Application.NONE)
        );

        boolean closeApplication = isCommandLine;
        if (runOnStart) {
            boolean successfulRun = app.runProcessor(null);
            closeApplication = closeApplication && !successfulRun;
        } else if (verifyOnStart) {
            app.verifyProgram(null);
        } else if (obfuscateOnStart) {
            app.obfuscateProgram(null);
        }

        if (closeApplication) app.close(null);
    }

}

