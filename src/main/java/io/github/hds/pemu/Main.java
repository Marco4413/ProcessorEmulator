package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.memory.flags.DummyMemoryFlag;
import io.github.hds.pemu.memory.registers.DummyMemoryRegister;
import io.github.hds.pemu.processor.DummyProcessor;
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

        // This could also create a full processor if you don't want to create a Dummy one
        app.setDummyProducer(
                cfg -> {
                    // Creating all registers that are present on a Processor
                    DummyMemoryRegister[] registers = new DummyMemoryRegister[Processor.IMPLEMENTED_REGISTERS.length];
                    for (int i = 0; i < Processor.IMPLEMENTED_REGISTERS.length; i++)
                        registers[i] = new DummyMemoryRegister(null, Processor.IMPLEMENTED_REGISTERS[i]);

                    // Creating all flags that are present on a Processor
                    DummyMemoryFlag[] flags = new DummyMemoryFlag[Processor.IMPLEMENTED_FLAGS.length];
                    for (int i = 0; i < Processor.IMPLEMENTED_FLAGS.length; i++)
                        flags[i] = new DummyMemoryFlag(null, Processor.IMPLEMENTED_FLAGS[i]);

                    return new DummyProcessor(cfg, registers, flags);
                }
        );

        app.setCurrentProgram(new File((String) parser.getOption("-program").getValue()));
        if ((boolean) parser.getOption("-run").getValue())
            app.runProcessor(null);
        app.setVisible(true);
    }

}

