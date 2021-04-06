package io.github.hds.pemu.compiler;

import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.utils.StringUtils;
import io.github.hds.pemu.utils.Token;
import io.github.hds.pemu.utils.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Compiler {

    protected static class Tokens {

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

    protected static class LabelData {
        public static int NULL_PTR = -1;

        public int pointer = NULL_PTR;
        public ArrayList<Integer> occurrences = new ArrayList<>();
        public LabelData() { }

        public LabelData(int pointer) {
            this.pointer = pointer;
        }
    }

    protected static class CompilerData {
        @NotNull ArrayList<Integer> program;
        @NotNull HashMap<String, LabelData> labels;
        @NotNull HashMap<String, Integer> constants;
        @NotNull Tokenizer tokenizer;
        int currentLine;

        protected CompilerData() {
            program = new ArrayList<>();
            labels = new HashMap<>();
            constants = Constants.getDefaultConstants();
            tokenizer = new Tokenizer();
            currentLine = 0;
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

    protected enum PARSE_STATUS {
        SUCCESS, FAIL, SUCCESS_PROGRAM_NOT_CHANGED
    }

    private static PARSE_STATUS parseLabel(@NotNull CompilerData cd, boolean declareOnly) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return PARSE_STATUS.FAIL;

        String nextToken = cd.tokenizer.peekNext(Tokens.SPACE);
        if (Tokens.LABEL.equals(nextToken)) {
            // If a label is being declared consume the declaration token
            cd.tokenizer.consumeNext(Tokens.SPACE);
            if (cd.labels.containsKey(lastToken)) {
                // If a label was already created check if it has a pointer
                LabelData labelData = cd.labels.get(lastToken);
                if (labelData.pointer == LabelData.NULL_PTR)
                    labelData.pointer = cd.program.size();
                else
                    // If the label has a valid pointer then it was already declared!
                    throw new TypeError("Label '" + lastToken + "' was already declared", cd.currentLine, cd.tokenizer.getConsumedCharacters());
            } else
                // If no label was created then create it
                cd.labels.put(lastToken, new LabelData(cd.program.size()));
            return PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED;
        } else if (!declareOnly) {
            if (cd.labels.containsKey(lastToken)) {
                LabelData labelData = cd.labels.get(lastToken);
                labelData.occurrences.add(cd.program.size());
            } else {
                LabelData labelData = new LabelData();
                labelData.occurrences.add(cd.program.size());
                cd.labels.put(lastToken, labelData);
            }
            cd.program.add(0);
        } else throw new SyntaxError("label declaration", lastToken, cd.currentLine, cd.tokenizer.getConsumedCharacters());

        return PARSE_STATUS.SUCCESS;
    }

    private static PARSE_STATUS parseConstant(@NotNull CompilerData cd, boolean isGetting) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return PARSE_STATUS.FAIL;

        if (Tokens.CONSTANT.equals(lastToken)) {
            if (isGetting) {
                String nextToken = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (nextToken == null) throw new SyntaxError("constant's name", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());
                else if (cd.constants.containsKey(nextToken)) {
                    // If the constant was defined put its value and consume the token
                    cd.tokenizer.consumeNext(Tokens.SPACE);
                    cd.program.add(cd.constants.get(nextToken));
                } else throw new ReferenceError("Constant", nextToken, cd.currentLine, cd.tokenizer.getConsumedCharacters());
            } else {
                String constantName = cd.tokenizer.consumeNext(Tokens.SPACE);
                String constantValue = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (constantName == null) throw new SyntaxError("constant's name", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());
                else if (constantValue == null) throw new SyntaxError("static number", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());

                try {
                    cd.constants.put(constantName, StringUtils.parseInt(constantValue));
                } catch (Exception err) {
                    throw new SyntaxError("static number", constantValue, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                }

                return PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED;
            }
        } else return PARSE_STATUS.FAIL;

        return PARSE_STATUS.SUCCESS;
    }

    private static PARSE_STATUS parseNumber(@NotNull CompilerData cd) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return PARSE_STATUS.FAIL;

        try {
            int value = StringUtils.parseInt(lastToken);
            cd.program.add(value);
        } catch (Exception err) {
            return PARSE_STATUS.FAIL;
        }

        return PARSE_STATUS.SUCCESS;
    }

    private static PARSE_STATUS parseAll(@NotNull CompilerData cd) {
        // Get the value to parse
        String valueToParse = cd.tokenizer.consumeNext(Tokens.SPACE);
        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new SyntaxError("value", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());

        PARSE_STATUS lastStatus = parseNumber(cd);
        if (lastStatus == PARSE_STATUS.FAIL) lastStatus = parseConstant(cd, true);
        if (lastStatus == PARSE_STATUS.FAIL) lastStatus = parseLabel(cd, false);
        return lastStatus;
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

        CompilerData cd = new CompilerData();

        for (cd.currentLine = 1; reader.hasNextLine(); cd.currentLine++) {
            cd.tokenizer = new Tokenizer(reader.nextLine(), true, Tokens.ALL_TOKENS);
            cd.tokenizer.removeEmpties();

            while (cd.tokenizer.hasNext()) {

                String token = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (token == null || Tokens.COMMENT.equals(token)) break;

                int instructionCode = instructionSet.getKeyCode(token);
                Instruction instruction = instructionSet.getInstruction(instructionCode);

                if (instruction != null) {
                    // If an instruction was found add it to the memory
                    cd.program.add(instructionCode);
                    for (int i = 0; i < instruction.ARGUMENTS;)
                        if (parseAll(cd) == PARSE_STATUS.SUCCESS) i++;

                } else if (Tokens.COMPILER.equals(token)) {
                    // Parsing Compiler Instructions
                    String compilerInstr = cd.tokenizer.consumeNext(Tokens.SPACE);
                    if (compilerInstr == null)
                        throw new SyntaxError("compiler instruction", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());
                    else if (compilerInstr.equals("DW")) {
                        parseAll(cd);
                    } else if (compilerInstr.equals("DS")) {
                        String terminator = cd.tokenizer.consumeNext(Tokens.SPACE);
                        if (Tokens.STRING.equals(terminator)) {
                            boolean escapeChar = false;
                            StringBuilder value = new StringBuilder();
                            while (true) {
                                String valueToAdd = cd.tokenizer.consumeNext();
                                if (valueToAdd == null)
                                    throw new SyntaxError("String terminator ('" + terminator + "')", String.valueOf(value.charAt(value.length() - 1)), cd.currentLine, cd.tokenizer.getConsumedCharacters());
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
                            for (int i = 0; i < value.length(); i++) cd.program.add((int) value.charAt(i));
                        } else throw new SyntaxError("String", terminator == null ? "null" : terminator, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                    } else if (compilerInstr.equals("DA")) {
                        // Be sure that there's the character that starts the array
                        String arrayStart = cd.tokenizer.consumeNext(Tokens.SPACE);
                        if (Tokens.ARR_START.equals(arrayStart)) {
                            while (true) {
                                // For each value in the array, check if the next value is the array closer character
                                String nextToken = cd.tokenizer.peekNext(Tokens.SPACE);
                                if (Tokens.ARR_END.equals(nextToken)) {
                                    cd.tokenizer.consumeNext(Tokens.SPACE);
                                    break;
                                }

                                try {
                                    parseAll(cd);
                                } catch (Exception err) {
                                    throw new SyntaxError("Array terminator ('}')", cd.tokenizer.getLast() == null ? "null" : cd.tokenizer.getLast(), cd.currentLine, cd.tokenizer.getConsumedCharacters());
                                }
                            }
                        } else throw new SyntaxError("Array", arrayStart == null ? "null" : arrayStart, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                    } else throw new SyntaxError("compiler instruction", compilerInstr, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                } else {
                    // Parsing Labels and Constants
                    if (parseConstant(cd, false) == PARSE_STATUS.FAIL) parseLabel(cd, true);
                }
            }
        }

        cd.labels.forEach((key, data) -> {
            if (data.pointer == LabelData.NULL_PTR) throw new IllegalStateException("Label '" + key + "' was never declared.");
            for (int occurrence : data.occurrences) {
                cd.program.set(occurrence, data.pointer);
            }
        });

        int[] primitiveIntProgram = new int[cd.program.size()];
        for (int i = 0; i < cd.program.size(); i++)
            primitiveIntProgram[i] = cd.program.get(i);
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
