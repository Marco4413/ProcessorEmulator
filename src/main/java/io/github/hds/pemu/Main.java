package io.github.hds.pemu;

import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.arguments.ArgumentsParser;

public class Main {

    public static void main(String[] args) {
        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineStr("-program", "-p", "/example.pemu")
              .defineInt("-pmem", "-pm", 64)
              .defineInt("-dmem", "-dm", 64);
        // Parse arguments
        parser.parse(args);

        // Check if help option was specified
        if ((Boolean) parser.getOption("-help").value) {
            System.err.println("PEMU [options]:\n" + parser.getUsage());
            return;
        }

        // Get specified program path and capacities
        String programPath = (String) parser.getOption("-program").value;
        int dataMemory     = (int) parser.getOption("-dmem").value;
        int programMemory  = (int) parser.getOption("-pmem").value;

        // Create a new Processor with the specified memory capacity
        Processor proc = new Processor(dataMemory, programMemory);

        // Compile the specified program
        int[] compiledProgram;
        try {
            compiledProgram = Compiler.compileFile(programPath, proc);
        } catch (Exception err) {
            System.err.println("Compilation error! (for " + programPath + ")");
            err.printStackTrace();
            return;
        }

        // Load compiled program into memory
        try {
            proc.PROGRAM.setValuesAt(0, compiledProgram);
        } catch (Exception err) {
            System.err.println("Error while loading program into memory!");
            err.printStackTrace();
            return;
        }

        // Run the processor
        try {
            proc.run();
        } catch (Exception err) {
            System.err.println("Error while running program!");
            err.printStackTrace();
        }
    }

}

