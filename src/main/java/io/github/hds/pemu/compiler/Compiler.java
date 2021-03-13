package io.github.hds.pemu.compiler;

import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Compiler {

    private static class LabelData {
        public int pointer = -1;
        public ArrayList<Integer> occurrences = new ArrayList<>();

        public LabelData() { }

        public LabelData(int pointer) {
            this.pointer = pointer;
        }
    }

    private static void parseConstant(@NotNull ArrayList<Integer> program, @NotNull HashMap<String, Integer> constants, @NotNull Tokenizer tokenizer, boolean assign) {
        String constName = tokenizer.consumeNext(Tokens.SPACE);
        if (constName == null) throw new IllegalStateException("No name specified for constant!");

        if (assign) {
            String value = tokenizer.consumeNext(Tokens.SPACE);
            if (value == null) throw new IllegalStateException("Constant: " + constName + " value wasn't specified!");

            try {
                constants.put(constName, StringUtils.parseInt(value));
            } catch (Exception err) {
                throw new IllegalStateException("Invalid value specified for constant: " + constName);
            }
        } else {
            if (constants.containsKey(constName))
                program.add(constants.get(constName));
            else throw new IllegalStateException("Constant: " + constName + " was never assigned before!");
        }
    }

    private static void parseLabel(@NotNull ArrayList<Integer> program, @NotNull HashMap<String, LabelData> labels, @NotNull Tokenizer tokenizer, boolean assign) {
        String labelName = tokenizer.consumeNext(Tokens.SPACE);
        if (labelName == null) throw new IllegalStateException("No name specified for label!");

        LabelData data = labels.containsKey(labelName) ? labels.get(labelName) : new LabelData();
        if (assign)
            data.pointer = program.size();
        else {
            data.occurrences.add(program.size());
            program.add(0);
        }
        labels.put(labelName, data);
    }

    public static int[] compileFile(@NotNull String resourcePath, @NotNull Processor processor) {
        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;

        InputStream stream = System.class.getResourceAsStream(resourcePath);
        if (stream == null) throw new IllegalArgumentException("Invalid Resource Path!");

        Scanner reader = new Scanner(stream);

        ArrayList<Integer> program = new ArrayList<>();
        HashMap<String, LabelData>  labels = new HashMap<>();
        HashMap<String, Integer> constants = new HashMap<>();

        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();

            Tokenizer tokenizedLine = new Tokenizer(line, true, Tokens.TOKENIZER_FILTER);
            tokenizedLine.removeDuplicates();
            Token currentState = Tokens.SPACE;

            while (true) {
                String token = tokenizedLine.consumeNext(Tokens.DELIMITERS);
                if (token == null || Tokens.COMMENT.equals(token)) break;

                if (currentState.equals(Tokens.INSTR)) {
                    if (Tokens.LABEL.equals(token))
                        parseLabel(program, labels, tokenizedLine, false);
                    else if (Tokens.CONSTANT.equals(token))
                        parseConstant(program, constants, tokenizedLine, false);
                    else
                        program.add(StringUtils.parseInt(token));
                } else {
                    if (Tokens.LABEL.equals(token)) {
                        parseLabel(program, labels, tokenizedLine, true);
                        currentState = Tokens.LABEL;
                    } else if (Tokens.CONSTANT.equals(token)) {
                        parseConstant(program, constants, tokenizedLine, true);
                        currentState = Tokens.CONSTANT;
                    } else {
                        int instructionCode = processor.INSTRUCTIONSET.getKeyCode(token);
                        if (instructionCode >= 0) {
                            program.add(instructionCode);
                            currentState = Tokens.INSTR;
                        }
                    }
                }
            }
        }

        labels.forEach((key, data) -> {
            if (data.pointer < 0) throw new IllegalStateException("Label: " + key + " was never defined!");
            for (int occurrence : data.occurrences) {
                program.set(occurrence, data.pointer);
            }
        });

        int[] primitiveIntProgram = new int[program.size()];
        for (int i = 0; i < program.size(); i++)
            primitiveIntProgram[i] = program.get(i);
        return primitiveIntProgram;
    }

}
