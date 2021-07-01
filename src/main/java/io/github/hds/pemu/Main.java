package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.config.ConfigManager;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.ProcessorConfig;

import javax.swing.*;
import java.io.File;

public final class Main {

    public static void main(String[] args) {

        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineFlag("-run", "-r")
              .defineRangedInt("-bits", "-b", ProcessorConfig.DEFAULT_BITS, ProcessorConfig.MIN_BITS, ProcessorConfig.MAX_BITS)
              .defineRangedInt("-memory", "-mem", ProcessorConfig.DEFAULT_MEMORY_SIZE, ProcessorConfig.MIN_MEMORY_SIZE, ProcessorConfig.MAX_MEMORY_SIZE)
              .defineRangedInt("-clock", "-c", ProcessorConfig.DEFAULT_CLOCK, ProcessorConfig.MIN_CLOCK, ProcessorConfig.MAX_CLOCK)
              .defineStr("-program", "-p", "");
        // Parse Arguments
        parser.parse(args);

        // If help flag was specified, print help and return
        if ((boolean) parser.getOption("-help").getValue()) {
            System.out.println("PEMU [options]:\n" + parser.getUsage());
            return;
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
        if (parser.isSpecified("-bits"))
            processorConfig.setBits((int) parser.getOption("-bits").getValue());
        if (parser.isSpecified("-memory"))
            processorConfig.setMemorySize((int) parser.getOption("-memory").getValue());
        if (parser.isSpecified("-clock"))
            processorConfig.setClock((int) parser.getOption("-clock").getValue());

        // Setting up app instance and showing it
        app.setProducer(Processor::new);
        app.setCurrentProgram(new File((String) parser.getOption("-program").getValue()));
        if ((boolean) parser.getOption("-run").getValue())
            app.runProcessor(null);
        app.setVisible(true);
    }

}

