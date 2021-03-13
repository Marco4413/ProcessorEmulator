package io.github.hds.pemu.compiler;

import io.github.hds.pemu.Processor;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Compiler {

    private enum LineType {
        NONE, INSTR, CONST, LABEL
    }

    private static int parseInt(@NotNull String str) {
        int radix = 10;
        String number = str;
             if (number.startsWith("0x")) radix = 16;
        else if (number.startsWith("0o")) radix =  8;
        else if (number.startsWith("0b")) radix =  2;
        if (radix != 10) number = number.substring(2);
        return Integer.parseInt(number, radix);
    }

    private static void parseConstant(@NotNull ArrayList<Integer> program, @NotNull HashMap<String, Integer> constants, @NotNull Tokenizer tokenizer, boolean assign) {
        String constName = tokenizer.consumeNext("\\s+");
        if (constName == null) throw new IllegalStateException("No name specified for constant!");

        if (assign) {
            String value = tokenizer.consumeNext("\\s+");
            if (value == null) throw new IllegalStateException("Constant: " + constName + " value wasn't specified!");

            try {
                constants.put(constName, parseInt(value));
            } catch (Exception err) {
                throw new IllegalStateException("Invalid value specified for constant: " + constName);
            }
        } else {
            if (constants.containsKey(constName))
                program.add(constants.get(constName));
            else throw new IllegalStateException("Constant: " + constName + " was never assigned before!");
        }
    }

    private static void parseLabel(@NotNull ArrayList<Integer> program, @NotNull HashMap<String, Integer> label, @NotNull Tokenizer tokenizer, boolean assign) {
        String labelName = tokenizer.consumeNext("\\s+");
        if (labelName == null) throw new IllegalStateException("No name specified for label!");

        if (assign)
            label.put(labelName, program.size());
        else {
            if (label.containsKey(labelName))
                program.add(label.get(labelName));
            else throw new IllegalStateException("Label: " + labelName + " was never assigned before!");
        }
    }

    public static int[] compileFile(@NotNull String resourcePath, @NotNull Processor processor) {
        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;

        InputStream stream = System.class.getResourceAsStream(resourcePath);
        if (stream == null) throw new IllegalArgumentException("Invalid Resource Path!");

        Scanner reader = new Scanner(stream);

        ArrayList<Integer> program = new ArrayList<>();
        HashMap<String, Integer> labels = new HashMap<>();
        HashMap<String, Integer> constants = new HashMap<>();

        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();

            Tokenizer tokenizedLine = new Tokenizer(line, true, "\\s", ",", ":", "@", ";");
            tokenizedLine.removeDuplicates();
            LineType currentLineType = LineType.NONE;

            while (tokenizedLine.hasNext()) {
                String token = tokenizedLine.consumeNext();
                     if (token.equals(";")) break;
                else if (token.matches("[\\s,]+")) continue;

                switch (currentLineType) {
                    case INSTR: {
                        if (token.equals(":")) {
                            parseLabel(program, labels, tokenizedLine, false);
                        } else if (token.equals("@")) {
                            parseConstant(program, constants, tokenizedLine, false);
                        } else
                            program.add(parseInt(token));
                        break;
                    }
                    case NONE: {
                        switch (token) {
                            case ":": {
                                parseLabel(program, labels, tokenizedLine, true);
                                currentLineType = LineType.LABEL;
                                break;
                            }
                            case "@": {
                                parseConstant(program, constants, tokenizedLine, true);
                                currentLineType = LineType.CONST;
                                break;
                            }
                            default: {
                                int instructionCode = processor.INSTRUCTIONSET.getKeyCode(token);
                                if (instructionCode >= 0) {
                                    program.add(instructionCode);
                                    currentLineType = LineType.INSTR;
                                }
                            }
                        }
                    }
                }
            }
        }

        int[] primitiveIntProgram = new int[program.size()];
        for (int i = 0; i < program.size(); i++)
            primitiveIntProgram[i] = program.get(i);
        return primitiveIntProgram;
    }

}
