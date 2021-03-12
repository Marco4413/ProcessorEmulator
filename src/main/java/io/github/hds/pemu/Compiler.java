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
            String[] tokens = line.split("[\\s,]+");
            for (String token : tokens) {
                int instructionCode = processor.INSTRUCTIONSET.getKeyCode(token);
                if (instructionCode >= 0)
                    program.add(instructionCode);
                else {
                    int radix = 10;
                    String number = token;
                         if (number.startsWith("0x")) radix = 16;
                    else if (number.startsWith("0o")) radix =  8;
                    else if (number.startsWith("0b")) radix =  2;
                    if (radix != 10) number = number.substring(2);
                    program.add(Integer.parseInt(number, radix));
                }
            }
        }

        int[] primitiveIntProgram = new int[program.size()];
        for (int i = 0; i < program.size(); i++)
            primitiveIntProgram[i] = program.get(i);
        return primitiveIntProgram;
    }

}
