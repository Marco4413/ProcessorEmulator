package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.app.Config;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.Clock;
import io.github.hds.pemu.processor.ProcessorConfig;
import io.github.hds.pemu.memory.Word;
import io.github.hds.pemu.utils.MathUtils;
import io.github.hds.pemu.utils.StringUtils;
import io.github.hds.pemu.utils.TranslationManager;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {

        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineFlag("-run", "-r")
              .defineRangedInt("-bits", "-b", Word.SizeBit16, Word.SizeBit8, Word.SizeBit24)
              .defineRangedInt("-memory", "-mem", 256, Byte.SIZE, Word.MaskBit24)
              .defineRangedInt("-clock", "-c", 1000, Clock.MIN_CLOCK, Clock.MAX_CLOCK)
              .defineStr("-program", "-p", "");
        // Parse Arguments
        parser.parse(args);

        // If help flag was specified, print help and return
        if ((boolean) parser.getOption("-help").value) {
            System.out.println("PEMU [options]:\n" + parser.getUsage());
            return;
        }

        // Setting System-based look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Creating initial processor config
        ProcessorConfig processorConfig = new ProcessorConfig(
                Word.getClosestSize((int) parser.getOption("-bits").value),
                MathUtils.makeMultipleOf(Byte.SIZE, (int) parser.getOption("-memory").value),
                (int) parser.getOption("-clock").value
        );

        // Trying to get an instance of the app
        Application app;
        try {
            app = Application.getInstance();
        } catch (Exception err) {
            // If it fails it's probably a bad config file, so we reset it
            Config appConfig = Config.getInstance();
            appConfig.resetToDefault();
            appConfig.saveConfig();
            // And try to get a new instance of the app, if this also fails, we let the program crash
            app = Application.getInstance();
        }

        // Setting up app instance and showing it
        app.setProcessorConfig(processorConfig);
        app.setCurrentProgram(new File((String) parser.getOption("-program").value));
        if ((boolean) parser.getOption("-run").value)
            app.runProcessor(null);
        app.setVisible(true);
    }

}

