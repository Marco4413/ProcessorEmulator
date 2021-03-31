package io.github.hds.pemu.compiler;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.utils.StringUtils;
import io.github.hds.pemu.utils.Token;
import io.github.hds.pemu.utils.Tokenizer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Compiler {

    public static class Tokens {

        public static final Token COMMENT   = new Token(";");
        public static final Token CONSTANT  = new Token("@");
        public static final Token LABEL     = new Token(":");
        public static final Token COMPILER  = new Token("#");
        public static final Token STRING    = new Token("\"'");
        public static final Token ESCAPECH  = new Token("\\\\");
        public static final Token ARR_START = new Token("{");
        public static final Token ARR_END   = new Token("}");
        public static final Token SPACE     = new Token("\\s");

        public static final Token[] ALL_TOKENS = new Token[] { COMMENT, CONSTANT, LABEL, COMPILER, STRING, ESCAPECH, ARR_START, ARR_END, SPACE };

    }

    public static class LabelData {
        public static int NULL_PTR = -1;

        public int pointer = NULL_PTR;
        public ArrayList<Integer> occurrences = new ArrayList<>();
        public LabelData() { }

        public LabelData(int pointer) {
            this.pointer = pointer;
        }
    }

    public static class SyntaxError extends RuntimeException {
        protected SyntaxError(@NotNull String expected, @NotNull String got, int currentLine, int currentChar) {
            super(String.format("Syntax Error (%d:%d): Expected %s, got '%s'.", currentLine, currentChar, expected, got));
        }
    }

    public static class ReferenceError extends RuntimeException {
        protected ReferenceError(@NotNull String type, @NotNull String name, int currentLine, int currentChar) {
            super(String.format("Reference Error (%d:%d): %s '%s' was not declared.", currentLine, currentChar, type, name));
        }
    }

    public static class TypeError extends RuntimeException {
        protected TypeError(@NotNull String message, int currentLine, int currentChar) {
            super(String.format("Type Error (%d:%d): %s.", currentLine, currentChar, message));
        }
    }

    private static boolean parseValue(ArrayList<Integer> program, HashMap<String, LabelData> labels, HashMap<String, Integer> constants, Tokenizer tokenizer, int currentLine) {
        // Default value is 0
        int value = 0;
        // Get the value to parse
        String valueToParse = tokenizer.consumeNext(Tokens.SPACE);

        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new SyntaxError("value", "null", currentLine, tokenizer.getConsumedCharacters());

        // Try to convert it into a number, if that didn't happen then the value must be either a label or constant
        try {
            value = StringUtils.parseInt(valueToParse);
        } catch (Exception err) {
            String nextToken = tokenizer.peekNext(Tokens.SPACE);
            if (Tokens.CONSTANT.equals(valueToParse)) {
                if (nextToken == null) throw new SyntaxError("constant's name", "null", currentLine, tokenizer.getConsumedCharacters());
                else if (constants.containsKey(nextToken)) {
                    // If the constant was defined put its value and consume the token
                    tokenizer.consumeNext(Tokens.SPACE);
                    value = constants.get(nextToken);
                } else throw new ReferenceError("Constant", nextToken, currentLine, tokenizer.getConsumedCharacters());
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
                        throw new TypeError("Label '" + valueToParse + "' was already declared", currentLine, tokenizer.getConsumedCharacters());
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

    public static int[] compileFile(@NotNull File file, @NotNull InstructionSet instructionSet) {
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

        for (int currentLine = 1; reader.hasNextLine(); currentLine++) {
            Tokenizer tokenizer = new Tokenizer(reader.nextLine(), true, Tokens.ALL_TOKENS);
            tokenizer.removeEmpties();

            while (tokenizer.hasNext()) {

                String token = tokenizer.consumeNext(Tokens.SPACE);
                if (token == null || Tokens.COMMENT.equals(token)) break;

                int instructionCode = instructionSet.getKeyCode(token);
                Instruction instruction = instructionSet.getInstruction(instructionCode);

                if (instruction != null) {
                    // If an instruction was found add it to the memory

                    program.add(instructionCode);
                    for (int i = 0; i < instruction.ARGUMENTS; ) {
                        if (parseValue(program, labels, constants, tokenizer, currentLine)) i++;
                    }
                } else if (Tokens.COMPILER.equals(token)) {
                    // Parsing Compiler Instructions

                    String compilerInstr = tokenizer.consumeNext(Tokens.SPACE);
                    if (compilerInstr == null)
                        throw new SyntaxError("compiler instruction", "null", currentLine, tokenizer.getConsumedCharacters());
                    else if (compilerInstr.equals("DW")) {
                        parseValue(program, labels, constants, tokenizer, currentLine);
                    } else if (compilerInstr.equals("DS")) {
                        String terminator = tokenizer.consumeNext(Tokens.SPACE);
                        if (Tokens.STRING.equals(terminator)) {
                            boolean escapeChar = false;
                            StringBuilder value = new StringBuilder();
                            while (true) {
                                String valueToAdd = tokenizer.consumeNext();
                                if (valueToAdd == null)
                                    throw new SyntaxError("String terminator ('" + terminator + "')", String.valueOf(value.charAt(value.length() - 1)), currentLine, tokenizer.getConsumedCharacters());
                                else if (escapeChar) {
                                    char escapedChar = valueToAdd.charAt(0);
                                    if (StringUtils.SpecialCharacters.MAP.containsKey(escapedChar)) {
                                        value.append(StringUtils.SpecialCharacters.MAP.get(escapedChar));
                                        if (valueToAdd.length() > 1)
                                            value.append(valueToAdd.substring(1));
                                    } else value.append(valueToAdd);
                                    escapeChar = false;
                                } else if (valueToAdd.equals(terminator)) break;
                                else if (Tokens.ESCAPECH.equals(valueToAdd)) escapeChar = true;
                                else value.append(valueToAdd);
                            }
                            for (int i = 0; i < value.length(); i++) program.add((int) value.charAt(i));
                        } else throw new SyntaxError("String", terminator == null ? "null" : terminator, currentLine, tokenizer.getConsumedCharacters());
                    } else if (compilerInstr.equals("DA")) {
                        // Be sure that there's the character that starts the array
                        String arrayStart = tokenizer.consumeNext(Tokens.SPACE);
                        if (Tokens.ARR_START.equals(arrayStart)) {
                            while (true) {
                                // For each value in the array, check if the next value is the array closer character
                                String nextToken = tokenizer.peekNext(Tokens.SPACE);
                                if (Tokens.ARR_END.equals(nextToken)) {
                                    tokenizer.consumeNext(Tokens.SPACE);
                                    break;
                                }

                                try {
                                    parseValue(program, labels, constants, tokenizer, currentLine);
                                } catch (Exception err) {
                                    throw new SyntaxError("Array terminator ('}')", tokenizer.getLast() == null ? "null" : tokenizer.getLast(), currentLine, tokenizer.getConsumedCharacters());
                                }
                            }
                        } else throw new SyntaxError("Array", arrayStart == null ? "null" : arrayStart, currentLine, tokenizer.getConsumedCharacters());
                    } else throw new SyntaxError("compiler instruction", compilerInstr, currentLine, tokenizer.getConsumedCharacters());
                } else {
                    // Parsing Labels and Constants
                    boolean isConstant = Tokens.CONSTANT.equals(token);
                    if (isConstant) {
                        String constantName = tokenizer.consumeNext(Tokens.SPACE);
                        String constantValue = tokenizer.consumeNext(Tokens.SPACE);
                        if (constantName == null) throw new SyntaxError("constant's name", "null", currentLine, tokenizer.getConsumedCharacters());
                        else if (constantValue == null) throw new SyntaxError("static number", "null", currentLine, tokenizer.getConsumedCharacters());

                        try {
                            constants.put(constantName, StringUtils.parseInt(constantValue));
                        } catch (Exception err) {
                            throw new SyntaxError("static number", constantValue, currentLine, tokenizer.getConsumedCharacters());
                        }
                    } else {
                        boolean isLabel = Tokens.LABEL.equals(tokenizer.consumeNext(Tokens.SPACE));
                        if (isLabel) {
                            if (labels.containsKey(token)) {
                                LabelData labelData = labels.get(token);
                                if (labelData.pointer == LabelData.NULL_PTR)
                                    labelData.pointer = program.size();
                                else
                                    throw new TypeError("Label '" + token + "' was already declared", currentLine, tokenizer.getConsumedCharacters());
                            } else
                                labels.put(token, new LabelData(program.size()));
                        } else throw new SyntaxError("label declaration", token, currentLine, tokenizer.getConsumedCharacters());
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

    public static @NotNull String obfuscateProgram(int[] program) {
        switch (program.length) {
            case 0: return "";
            case 1: return "#DW " + program[0];
            default: {
                StringBuilder obfProgram = new StringBuilder();
                obfProgram.append("#DA { ");
                for (int value : program) {
                    obfProgram.append(value).append(' ');
                }
                obfProgram.append("}");
                return obfProgram.toString();
            }
        }
    }

}
