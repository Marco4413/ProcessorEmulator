package io.github.hds.pemu;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {

    public static int[] compileFile(@NotNull String resourcePath, @NotNull Processor processor) {
        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;

        InputStream stream = System.class.getResourceAsStream(resourcePath);
        if (stream == null) throw new IllegalArgumentException("Invalid Resource Path!");

        Scanner reader = new Scanner(stream);

        ArrayList<Integer> program = new ArrayList<>();

        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();
            String[] tokens = line.split("\t");
            for (String token : tokens) {
                int instructionCode = processor.INSTRUCTIONSET.getKeyCode(token);
                if (instructionCode >= 0)
                    program.add(instructionCode);
                else
                    program.add(Integer.parseInt(token));
            }
        }

        int[] primitiveIntProgram = new int[program.size()];
        for (int i = 0; i < program.size(); i++)
            primitiveIntProgram[i] = program.get(i);
        return primitiveIntProgram;
    }

}
