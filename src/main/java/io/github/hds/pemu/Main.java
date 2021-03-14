package io.github.hds.pemu;

import io.github.hds.pemu.compiler.Compiler;
import io.github.hds.pemu.arguments.ArgumentsParser;
import io.github.hds.pemu.processor.Processor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class Main {

    private static void printUsage(@NotNull ArgumentsParser parser) {
        System.err.println("PEMU PROGRAM_PATH [options]:\n" + parser.getUsage());
    }

    public static void main(String[] args) {
        // Create new arguments parser
        ArgumentsParser parser = new ArgumentsParser();
        // Define valid options
        parser.defineFlag("-help", "-h")
              .defineInt("-bits", "-b", 16)
              .defineInt("-memory", "-mem", 256);

        // If no program was specified, print usage
        if (args.length == 0) {
            printUsage(parser);
            return;
        }

        // Get program  path
        String programPath = args[0];
        File programFile = new File(programPath);

        // If options were specified, parse them
        if (args.length > 1)
            parser.parse(Arrays.copyOfRange(args, 1, args.length));

        // Check if help option was specified
        if ((Boolean) parser.getOption("-help").value) {
            printUsage(parser);
            return;
        }

        // Get specified program path and capacities
        int wordSize       = (int) parser.getOption("-bits").value;
        int memoryCapacity = (int) parser.getOption("-memory").value;

        // Create a new Processor with the specified values
        Processor proc;
        try {
            proc = new Processor(wordSize, memoryCapacity);
        } catch (Exception err) {
            System.err.println("Couldn't create processor.");
            err.printStackTrace();
            return;
        }

        // Compile the specified program
        int[] compiledProgram;
        try {
            compiledProgram = Compiler.compileFile(new File(args[0]), proc);
        } catch (Exception err) {
            System.err.println("Compilation error. (for file @'" + programFile.getAbsolutePath() + "')");
            err.printStackTrace();
            return;
        }

        // Load compiled program into memory
        try {
            proc.MEMORY.setValuesAt(0, compiledProgram);
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

