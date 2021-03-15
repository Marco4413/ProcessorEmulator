package io.github.hds.pemu;

import io.github.hds.pemu.app.Application;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.ProcessorConfig;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {

        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineFlag("-run", "-r")
              .defineInt("-bits", "-b", 16)
              .defineInt("-memory", "-mem", 256)
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
        ProcessorConfig config = new ProcessorConfig(
                (int) parser.getOption("-bits").value,
                (int) parser.getOption("-memory").value
        );

        // Initializing app instance and showing it
        Application app = Application.getInstance(config);
        app.setCurrentProgram(new File((String) parser.getOption("-program").value));
        if ((boolean) parser.getOption("-run").value)
            app.runProcessor(null);
        app.setVisible(true);
    }

}

