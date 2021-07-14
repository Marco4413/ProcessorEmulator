package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.app.Console;
import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.ProcessorConfig;

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
              .defineFlag("--run", "-r")
              .defineFlag("--command-line", "-cl")
              .defineRangedInt("--bits", "-b", ProcessorConfig.DEFAULT_BITS, ProcessorConfig.MIN_BITS, ProcessorConfig.MAX_BITS)
              .defineRangedInt("--memory", "-mem", ProcessorConfig.DEFAULT_MEMORY_SIZE, ProcessorConfig.MIN_MEMORY_SIZE, ProcessorConfig.MAX_MEMORY_SIZE)
              .defineRangedInt("--clock", "-c", ProcessorConfig.DEFAULT_CLOCK, ProcessorConfig.MIN_CLOCK, ProcessorConfig.MAX_CLOCK)
              .defineStr("--program", "-p", "");
        // Parse Arguments
        parser.parse(args);

        // If help flag was specified, print help and return
        if (parser.isSpecified("--help")) {
            System.out.println("PEMU [options]:\n" + parser.getUsage());
            return;
        }

        boolean isCommandLine = parser.isSpecified("--command-line");
        boolean autoRun = parser.isSpecified("--run");

        // If the user wants the program to run as a console app
        if (isCommandLine) {
            // Auto run must be specified, because otherwise the program wouldn't run
            if (!autoRun) {
                System.err.println("\"--run\" flag must be specified with the \"--command-line\" flag");
                return;
            }

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
        if (parser.isSpecified("--memory"))
            processorConfig.setMemorySize((int) parser.getOption("--memory").getValue());
        if (parser.isSpecified("--clock"))
            processorConfig.setClock((int) parser.getOption("--clock").getValue());

        // Setting up app instance and showing it
        app.setProducer(Processor::new);

        // If not specified the app will create a full Processor instead
        //  This is used when verifying or obfuscating code
        app.setDummyProducer(Processor::getDummyProcessor);

        app.setCurrentProgram(new File((String) parser.getOption("--program").getValue()));

        // Setting the app visible only if not on command line
        app.setVisible(!isCommandLine);

        // Setting app's flags to prevent setVisible to change the app's visibility
        //  and making it close on Processor Stop if on command line
        app.setFlags(
                  (isCommandLine ? Application.CLOSE_ON_PROCESSOR_STOP : Application.NONE)
                | Application.PREVENT_VISIBILITY_CHANGE
        );

        if (autoRun) {
            boolean successfulRun = app.runProcessor(null);
            if (isCommandLine && !successfulRun) app.close(null);
        }
    }

}

