package io.github.hds.pemu;

import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.Processor;

public class Main {

    public static void main(String[] args) {
        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineStr("-program", "-p", "/example.pemu")
              .defineInt("-bits", "-b", 16)
              .defineInt("-pmem", "-pm", 128)
              .defineInt("-dmem", "-dm", 128);
        // Parse arguments
        parser.parse(args);

        // Check if help option was specified
        if ((Boolean) parser.getOption("-help").value) {
            System.err.println("PEMU [options]:\n" + parser.getUsage());
            return;
        }

        // Get specified program path and capacities
        String programPath = (String) parser.getOption("-program").value;
        int wordSize       = (int) parser.getOption("-bits").value;
        int dataMemory     = (int) parser.getOption("-dmem").value;
        int programMemory  = (int) parser.getOption("-pmem").value;

        // Create a new Processor with the specified values
        Processor proc;
        try {
            proc = new Processor(wordSize, dataMemory, programMemory);
        } catch (Exception err) {
            System.err.println("Couldn't create processor!");
            err.printStackTrace();
            return;
        }

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
            System.out.println("Running Processor:\n" + proc.getInfo());
            proc.run();
        } catch (Exception err) {
            System.err.println("Error while running program!");
            err.printStackTrace();
        }
    }

}

