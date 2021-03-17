package io.github.hds.pemu.compiler;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.processor.Processor;
import io.github.hds.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Compiler {

    private static class LabelData {
        public static int NULL_PTR = -1;

        public int pointer = NULL_PTR;
        public ArrayList<Integer> occurrences = new ArrayList<>();

        public LabelData() { }

        public LabelData(int pointer) {
            this.pointer = pointer;
        }
    }

    public static boolean parseValue(ArrayList<Integer> program, HashMap<String, LabelData> labels, HashMap<String, Integer> constants, Tokenizer tokenizer) {
        // Default value is 0
        int value = 0;
        // Get the value to parse
        String valueToParse = tokenizer.consumeNext(Tokens.SPACE);

        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new IllegalStateException("Expected value got null.");

        // Try to convert it into a number, if that didn't happen then the value must be either a label or constant
        try {
            value = StringUtils.parseInt(valueToParse);
        } catch (Exception err) {
            String nextToken = tokenizer.peekNext(Tokens.SPACE);
            if (Tokens.CONSTANT.equals(valueToParse)) {
                if (nextToken == null) throw new IllegalStateException("Expected constant's name, got null.");
                else if (constants.containsKey(nextToken)) {
                    // If the constant was defined put its value and consume the token
                    tokenizer.consumeNext(Tokens.SPACE);
                    program.add(constants.get(nextToken));
                } else throw new IllegalStateException("Constant '" + nextToken + "' was never declared before.");
            } else if (Tokens.LABEL.equals(nextToken)) {
                // If a label is being declared consume the declaration token
                tokenizer.consumeNext(Tokens.SPACE);
                if (labels.containsKey(valueToParse)) {
                    // If a label was already created check if it has a pointer
                    LabelData labelData = labels.get(valueToParse);
                    if (labelData.pointer == LabelData.NULL_PTR)
                        labelData.pointer = program.size();
                    else
                        // If the label has a valid pointer then it was already declared!
                        throw new IllegalStateException("Label '" + valueToParse + "' was already declared.");
                } else
                    // If no label was created then create it
                    labels.put(valueToParse, new LabelData(program.size()));
                return false; // Returning false tells the compiler that no value was added to the program

            // If no label is being declared put the label's value
            } else if (labels.containsKey(valueToParse)) {
                LabelData labelData = labels.get(valueToParse);
                labelData.occurrences.add(program.size());
            } else {
                LabelData labelData = new LabelData();
                labelData.occurrences.add(program.size());
                labels.put(valueToParse, labelData);
            }
        }

        program.add(value);
        return true;
    }

    public static int[] compileFile(@NotNull File file, @NotNull Processor processor) {
        if (!file.exists()) throw new IllegalArgumentException("'" + file.getAbsolutePath() + "': The specified file doesn't exist.");
        if (!file.canRead()) throw new IllegalArgumentException("'" + file.getAbsolutePath() + "': The specified file can't be read.");

        Scanner reader;
        try {
            reader = new Scanner(file);
        } catch (Exception err) {
            throw new IllegalStateException("Something went wrong while initializing Scanner.");
        }

        ArrayList<Integer> program = new ArrayList<>();
        HashMap<String, LabelData>  labels = new HashMap<>();
        HashMap<String, Integer> constants = Constants.getDefaultConstants(); // Getting Default constants

        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();

            Tokenizer tokenizer = new Tokenizer(line, true, Tokens.ALL_TOKENS);
            tokenizer.removeEmpties();

            while (tokenizer.hasNext()) {

                String token = tokenizer.consumeNext(Tokens.SPACE);
                if (token == null || Tokens.COMMENT.equals(token)) break;

                int instructionCode = processor.INSTRUCTIONSET.getKeyCode(token);
                Instruction instruction = processor.INSTRUCTIONSET.getInstruction(instructionCode);

                if (instruction != null) {
                    // If an instruction was found add it to the memory

                    program.add(instructionCode);
                    for (int i = 0; i < instruction.ARGUMENTS; ) {
                        if (parseValue(program, labels, constants, tokenizer)) i++;
                    }
                } else if (Tokens.COMPILER.equals(token)) {
                    // Parsing Compiler Instructions

                    String compilerInstr = tokenizer.consumeNext(Tokens.SPACE);
                    if (compilerInstr == null)
                        throw new IllegalStateException("Expected compiler instruction, got null.");
                    else if (compilerInstr.equals("DW")) {
                        parseValue(program, labels, constants, tokenizer);
                    } else if (compilerInstr.equals("DS")) {
                        String terminator = tokenizer.consumeNext(Tokens.SPACE);
                        if (Tokens.STRING.equals(terminator)) {
                            boolean escapeChar = false;
                            StringBuilder value = new StringBuilder();
                            while (true) {
                                String valueToAdd = tokenizer.consumeNext();
                                if (valueToAdd == null)
                                    throw new IllegalStateException("String '" + value.toString() + "' wasn't terminated properly.");
                                else if (escapeChar) {
                                    char escapedChar = valueToAdd.charAt(0);
                                    if (SpecialCharacters.SPECIAL_MAP.containsKey(escapedChar)) {
                                        value.append(SpecialCharacters.SPECIAL_MAP.get(escapedChar));
                                        if (valueToAdd.length() > 1)
                                            value.append(valueToAdd.substring(1));
                                    } else value.append(valueToAdd);
                                    escapeChar = false;
                                } else if (valueToAdd.equals(terminator)) break;
                                else if (Tokens.ESCAPECH.equals(valueToAdd)) escapeChar = true;
                                else value.append(valueToAdd);
                            }
                            for (int i = 0; i < value.length(); i++) program.add((int) value.charAt(i));
                        } else throw new IllegalStateException("String expected, got '" + terminator + "'");
                    } else throw new IllegalStateException("Expected compiler instruction, got '" + compilerInstr + "'.");
                } else {
                    // Parsing Labels and Constants
                    boolean isConstant = Tokens.CONSTANT.equals(token);
                    if (isConstant) {
                        String constantName = tokenizer.consumeNext(Tokens.SPACE);
                        String constantValue = tokenizer.consumeNext(Tokens.SPACE);
                        if (constantName == null) throw new IllegalStateException("Expected constant's name, got null.");
                        else if (constantValue == null) throw new IllegalStateException("Expected value for constant '" + constantName + "', got null.");

                        try {
                            constants.put(constantName, StringUtils.parseInt(constantValue));
                        } catch (Exception err) {
                            throw new IllegalStateException("Expected static number for constant '" + constantName + "', got '" + constantValue + "'.");
                        }
                    } else {
                        boolean isLabel = Tokens.LABEL.equals(tokenizer.consumeNext(Tokens.SPACE));
                        if (isLabel) {
                            if (labels.containsKey(token)) {
                                LabelData labelData = labels.get(token);
                                if (labelData.pointer == LabelData.NULL_PTR)
                                    labelData.pointer = program.size();
                                else
                                    throw new IllegalStateException("Label '" + token + "' was already declared.");
                            } else
                                labels.put(token, new LabelData(program.size()));
                        } else throw new IllegalStateException("Expected label declaration, got '" + token + "'.");
                    }
                }
            }
        }

        labels.forEach((key, data) -> {
            if (data.pointer == LabelData.NULL_PTR) throw new IllegalStateException("Label '" + key + "' was never declared.");
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
