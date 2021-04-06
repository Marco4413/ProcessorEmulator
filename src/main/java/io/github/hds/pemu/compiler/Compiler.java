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
        public static final Token ESCAPE_CH = new Token("\\", true);
        public static final Token ARR_START = new Token("{");
        public static final Token ARR_END   = new Token("}");
        public static final Token OFF_START = new Token("[", true);
        public static final Token OFF_END   = new Token("]", true);
        public static final Token SPACE     = new Token("\\s");

        public static final Token[] ALL_TOKENS = new Token[] {
                COMMENT, CONSTANT, LABEL, COMPILER, STRING, ESCAPE_CH, ARR_START, ARR_END, OFF_START, OFF_END, SPACE
        };

    }

    protected static class LabelData {
        public static int NULL_PTR = -1;

        public int pointer = NULL_PTR;
        public ArrayList<Integer> occurrences = new ArrayList<>();
        public ArrayList<Integer> offsets = new ArrayList<>();
        public LabelData() { }

        public LabelData(int pointer) {
            this.pointer = pointer;
        }

        public void addOccurrence(int at) {
            addOccurrence(at, 0);
        }

        public void addOccurrence(int at, int offset) {
            occurrences.add(at);
            offsets.add(offset);
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
        protected SyntaxError(@NotNull String expected, @Nullable String got, int currentLine, int currentChar) {
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

    protected static class ParseResult {
        public final PARSE_STATUS STATUS;
        public final String NAME;
        public final int VALUE;

        protected ParseResult(PARSE_STATUS status) {
            this(status, null, -1);
        }

        protected ParseResult(PARSE_STATUS status, String name, int value) {
            STATUS = status;
            NAME = name;
            VALUE = value;
        }
    }

    private static ParseResult parseLabel(@NotNull CompilerData cd, boolean declareOnly) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return new ParseResult(PARSE_STATUS.FAIL);

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
            return new ParseResult(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, lastToken, cd.program.size());
        } else if (!declareOnly) {
            int offset = 0;
            if (Tokens.OFF_START.equals(nextToken)) {
                cd.tokenizer.consumeNext(Tokens.SPACE);
                String tokenToParse = cd.tokenizer.consumeNext(Tokens.SPACE);

                ParseResult lastResult = parseNumber(cd, false);
                if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(cd, true, false);
                if (lastResult.STATUS == PARSE_STATUS.FAIL) throw new SyntaxError("constant or static number", tokenToParse, cd.currentLine, cd.tokenizer.getConsumedCharacters());

                offset = lastResult.VALUE;

                String offEndToken = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (!Tokens.OFF_END.equals(offEndToken)) throw new SyntaxError("Offset terminator ('" + Tokens.OFF_END.PATTERN + "')", offEndToken, cd.currentLine, cd.tokenizer.getConsumedCharacters());
            }

            if (cd.labels.containsKey(lastToken)) {
                LabelData labelData = cd.labels.get(lastToken);
                labelData.addOccurrence(cd.program.size(), offset);
            } else {
                LabelData labelData = new LabelData();
                labelData.addOccurrence(cd.program.size(), offset);
                cd.labels.put(lastToken, labelData);
            }
            cd.program.add(0);
            return new ParseResult(PARSE_STATUS.SUCCESS, lastToken, cd.program.size());
        } else throw new SyntaxError("label declaration", lastToken, cd.currentLine, cd.tokenizer.getConsumedCharacters());
    }

    private static ParseResult parseConstant(@NotNull CompilerData cd, boolean isGetting, boolean addToProgram) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return new ParseResult(PARSE_STATUS.FAIL);

        if (Tokens.CONSTANT.equals(lastToken)) {
            String constantName = cd.tokenizer.consumeNext(Tokens.SPACE);
            if (isGetting) {
                if (constantName == null) throw new SyntaxError("constant's name", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());
                else if (cd.constants.containsKey(constantName)) {
                    // If the constant was defined save its value
                    if (addToProgram) cd.program.add(cd.constants.get(constantName));
                    return new ParseResult(addToProgram ? PARSE_STATUS.SUCCESS : PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, cd.constants.get(constantName));
                } else throw new ReferenceError("Constant", constantName, cd.currentLine, cd.tokenizer.getConsumedCharacters());
            } else {
                String constantValue = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (constantName == null) throw new SyntaxError("constant's name", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());
                else if (constantValue == null) throw new SyntaxError("static number", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());

                try {
                    int value = StringUtils.parseInt(constantValue);
                    cd.constants.put(constantName, value);
                    return new ParseResult(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, value);
                } catch (Exception err) {
                    throw new SyntaxError("static number", constantValue, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                }
            }
        } else return new ParseResult(PARSE_STATUS.FAIL);
    }

    private static ParseResult parseNumber(@NotNull CompilerData cd, boolean addToProgram) {
        String lastToken = cd.tokenizer.getLast();
        if (lastToken == null) return new ParseResult(PARSE_STATUS.FAIL);

        try {
            int value = StringUtils.parseInt(lastToken);
            if (addToProgram) cd.program.add(value);
            return new ParseResult(addToProgram ? PARSE_STATUS.SUCCESS : PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, value);
        } catch (Exception err) {
            return new ParseResult(PARSE_STATUS.FAIL);
        }
    }

    private static ParseResult parseAll(@NotNull CompilerData cd) {
        // Get the value to parse
        String valueToParse = cd.tokenizer.consumeNext(Tokens.SPACE);
        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new SyntaxError("value", "null", cd.currentLine, cd.tokenizer.getConsumedCharacters());

        ParseResult lastResult = parseNumber(cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(cd, true, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseLabel(cd, false);
        return lastResult;
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
                        if (parseAll(cd).STATUS == PARSE_STATUS.SUCCESS) i++;

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
                                else if (Tokens.ESCAPE_CH.equals(valueToAdd)) escapeChar = true;
                                else value.append(valueToAdd);
                            }
                            for (int i = 0; i < value.length(); i++) cd.program.add((int) value.charAt(i));
                        } else throw new SyntaxError("String", terminator, cd.currentLine, cd.tokenizer.getConsumedCharacters());
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
                                    throw new SyntaxError("Array terminator ('" + Tokens.ARR_END.PATTERN + "')", cd.tokenizer.getLast(), cd.currentLine, cd.tokenizer.getConsumedCharacters());
                                }
                            }
                        } else throw new SyntaxError("Array", arrayStart, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                    } else throw new SyntaxError("compiler instruction", compilerInstr, cd.currentLine, cd.tokenizer.getConsumedCharacters());
                } else {
                    // Parsing Labels and Constants
                    if (parseConstant(cd, false, true).STATUS == PARSE_STATUS.FAIL) parseLabel(cd, true);
                }
            }
        }

        cd.labels.forEach((key, data) -> {
            if (data.pointer == LabelData.NULL_PTR) throw new ReferenceError("Label", key, -1, -1);
            if (data.occurrences.size() != data.offsets.size()) throw new IllegalStateException("Label '" + key + "' has different amounts of occurrences and offsets.");
            for (int i = 0; i < data.occurrences.size(); i++)
                cd.program.set(data.occurrences.get(i), data.pointer + data.offsets.get(i));
        });

        int[] primitiveIntProgram = new int[cd.program.size()];
        for (int i = 0; i < cd.program.size(); i++)
            primitiveIntProgram[i] = cd.program.get(i);
        return primitiveIntProgram;
    }

    public static @NotNull String obfuscateProgram(int[] program) {
        switch (program.length) {
            case 0: return "; Nothing to see here ;)";
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
