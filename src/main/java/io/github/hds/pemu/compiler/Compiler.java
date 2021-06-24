package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.labels.BasicLabel;
import io.github.hds.pemu.compiler.labels.OffsetLabel;
import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.flags.MemoryFlag;
import io.github.hds.pemu.memory.registers.IRegister;
import io.github.hds.pemu.memory.registers.MemoryRegister;
import io.github.hds.pemu.processor.IProcessor;
import io.github.hds.pemu.tokenizer.TokenGroup;
import io.github.hds.pemu.utils.StringUtils;
import io.github.hds.pemu.tokenizer.Token;
import io.github.hds.pemu.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Compiler {

    protected static final String CI_DEFINE_WORD   = "DW";
    protected static final String CI_DEFINE_STRING = "DS";
    protected static final String CI_DEFINE_ARRAY  = "DA";

    protected static class Tokens {

        public static final Token COMMENT   = new Token(';');
        public static final Token CONSTANT  = new Token('@');
        public static final Token LABEL     = new Token(':');
        public static final Token COMPILER  = new Token('#');
        public static final Token STRING    = new Token('"', "\"'", false);
        public static final Token CHARACTER = new Token('\'');
        public static final Token ESCAPE_CH = new Token('\\', true);
        public static final Token ARR_START = new Token('{');
        public static final Token ARR_END   = new Token('}');
        public static final Token OFF_START = new Token('[', true);
        public static final Token OFF_END   = new Token(']', true);
        public static final Token SPACE     = new Token(' ', "\\s", false);
        public static final Token NEWLINE   = new Token('\n');
        public static final Token STR_CODEPOINT_TERMINATOR = new Token(';');

        // The class TokenGroup makes sure that no duplicate pattern is present,
        //  so it discards a Token if one that is equal is present, this should make Tokenizer faster
        public static final TokenGroup ALL_TOKENS = new TokenGroup().addTokens(
                COMMENT, CONSTANT, LABEL, COMPILER, STRING, CHARACTER, ESCAPE_CH, ARR_START, ARR_END, OFF_START, OFF_END, SPACE, NEWLINE, STR_CODEPOINT_TERMINATOR
        );

    }

    protected static class CompilerData {
        public final @NotNull IProcessor processor;
        public final @NotNull ArrayList<Integer> program;
        public final @NotNull LabelData<OffsetLabel> labels;
        public final @NotNull HashMap<String, Integer> constants;
        public final @NotNull RegisterData registers;
        public final @NotNull OffsetsData offsets;
        public final @NotNull Tokenizer tokenizer;

        protected CompilerData(@NotNull IProcessor processor, @NotNull Tokenizer tokenizer) {
            this.processor = processor;
            this.program = new ArrayList<>();
            this.labels = new LabelData<>();
            this.constants = Constants.getDefaultConstants();
            this.registers = new RegisterData();
            this.offsets = new OffsetsData();
            this.tokenizer = tokenizer;
        }
    }

    public static class SyntaxError extends RuntimeException {
        protected SyntaxError(@NotNull String expected, @Nullable String got, @NotNull Tokenizer tokenizer) {
            this(expected, got, tokenizer.getConsumedLines() + 1, tokenizer.getConsumedLineCharacters());
        }

        protected SyntaxError(@NotNull String expected, @Nullable String got, int currentLine, int currentChar) {
            super(String.format("Syntax Error (%d:%d): Expected %s, got '%s'.", currentLine, currentChar, expected, got));
        }
    }

    public static class ReferenceError extends RuntimeException {
        protected ReferenceError(@NotNull String type, @NotNull String name, @NotNull Tokenizer tokenizer) {
            this(type, name, tokenizer.getConsumedLines() + 1, tokenizer.getConsumedLineCharacters());
        }

        protected ReferenceError(@NotNull String type, @NotNull String name, int currentLine, int currentChar) {
            super(String.format("Reference Error (%d:%d): %s '%s' was not declared.", currentLine, currentChar, type, name));
        }
    }

    public static class TypeError extends RuntimeException {
        protected TypeError(@NotNull String message, @NotNull Tokenizer tokenizer) {
            this(message, tokenizer.getConsumedLines() + 1, tokenizer.getConsumedLineCharacters());
        }

        protected TypeError(@NotNull String message, int currentLine, int currentChar) {
            super(String.format("Type Error (%d:%d): %s.", currentLine, currentChar, message));
        }
    }

    public static class ProcessorError extends RuntimeException {
        protected ProcessorError(@NotNull String message, @NotNull Tokenizer tokenizer) {
            this(message, tokenizer.getConsumedLines() + 1, tokenizer.getConsumedLineCharacters());
        }

        protected ProcessorError(@NotNull String message, int currentLine, int currentChar) {
            super(String.format("Processor Error (%d:%d): %s.", currentLine, currentChar, message));
        }
    }

    protected enum PARSE_STATUS {
        SUCCESS, FAIL, SUCCESS_PROGRAM_NOT_CHANGED
    }

    protected static class ParseResult <T> {
        public final PARSE_STATUS STATUS;
        public final String NAME;
        public final T VALUE;

        protected ParseResult(PARSE_STATUS status) {
            this(status, null, null);
        }

        protected ParseResult(PARSE_STATUS status, String name, T value) {
            STATUS = status;
            NAME = name;
            VALUE = value;
        }
    }

    private static ParseResult<Integer> parseOffset(@NotNull CompilerData cd, boolean peekNext, @Nullable Token peekBlacklist, boolean addToProgram) {

        String offsetStart = peekNext ? (peekBlacklist == null ? cd.tokenizer.peekNext() : cd.tokenizer.peekNext(peekBlacklist)) : cd.tokenizer.getLast();
        if (offsetStart == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.OFF_START.matches(offsetStart)) {
            if (peekNext) cd.tokenizer.consumeNext(Tokens.SPACE);
            String offsetToParse = cd.tokenizer.consumeNext(Tokens.SPACE);

            ParseResult<Integer> lastResult = parseNumber(cd, false);
            if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(cd, true, false);
            if (lastResult.STATUS == PARSE_STATUS.FAIL) throw new SyntaxError("Constant or Number", offsetToParse, cd.tokenizer);

            int offset = lastResult.VALUE;

            String offsetEnd = cd.tokenizer.consumeNext(Tokens.SPACE);
            if (Tokens.OFF_END.matches(offsetEnd)) {
                if (addToProgram) {
                    cd.offsets.put(cd.program.size(), offset);
                    cd.program.add(0);
                    return new ParseResult<>(PARSE_STATUS.SUCCESS, null, offset);
                } else return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, offset);
            } else throw new SyntaxError("Offset terminator ('" + Tokens.OFF_END.getPattern() + "')", offsetEnd, cd.tokenizer);
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseLabel(@NotNull CompilerData cd, boolean declareOnly) {
        String labelName = cd.tokenizer.getLast();
        if (labelName == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        boolean isBeingDeclared = Tokens.LABEL.matches(
                cd.tokenizer.peekNext(Tokens.SPACE)
        );

        if (isBeingDeclared) {
            // If a label is being declared consume the declaration token
            cd.tokenizer.consumeNext(Tokens.SPACE);
            if (cd.labels.containsKey(labelName)) {
                // If a label was already created check if it has a pointer
                OffsetLabel label = cd.labels.get(labelName);
                if (label.getPointer() == OffsetLabel.NULL_PTR)
                    label.setPointer(cd.program.size());
                else
                    // If the label has a valid pointer then it was already declared!
                    throw new TypeError("Label '" + labelName + "' was already declared", cd.tokenizer);
            } else
                // If no label was created then create it
                cd.labels.put(labelName, new OffsetLabel().setPointer(cd.program.size()));
            return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, labelName, cd.program.size());
        } else if (!declareOnly) {
            int offset = 0;
            ParseResult<Integer> offsetResult = parseOffset(cd, true, null, false);
            if (offsetResult.STATUS != PARSE_STATUS.FAIL) offset = offsetResult.VALUE;

            if (cd.labels.containsKey(labelName)) {
                OffsetLabel label = cd.labels.get(labelName);
                label.addInstance(cd.program.size(), offset);
            } else {
                OffsetLabel label = new OffsetLabel();
                label.addInstance(cd.program.size(), offset);
                cd.labels.put(labelName, label);
            }
            cd.program.add(0);
            return new ParseResult<>(PARSE_STATUS.SUCCESS, labelName, cd.program.size());
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseConstant(@NotNull CompilerData cd, boolean isGetting, boolean addToProgram) {
        String constantPrefix = cd.tokenizer.getLast();
        if (constantPrefix == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.CONSTANT.matches(constantPrefix)) {
            String constantName = cd.tokenizer.consumeNext(Tokens.SPACE);
            if (isGetting) {
                if (constantName == null) throw new SyntaxError("Constant's name", "null", cd.tokenizer);
                else if (cd.constants.containsKey(constantName)) {
                    // If the constant was defined save its value
                    if (addToProgram) cd.program.add(cd.constants.get(constantName));
                    return new ParseResult<>(addToProgram ? PARSE_STATUS.SUCCESS : PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, cd.constants.get(constantName));
                } else throw new ReferenceError("Constant", constantName, cd.tokenizer);
            } else {
                String constantValue = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (constantName == null) throw new SyntaxError("Constant's name", "null", cd.tokenizer);
                else if (constantValue == null) throw new SyntaxError("Number, Character or Constant", "null", cd.tokenizer);

                ParseResult<Integer> result = parseNumber(cd, false);
                if (result.STATUS == PARSE_STATUS.FAIL) result = parseCharacter(cd, false);
                if (result.STATUS == PARSE_STATUS.FAIL) result = parseConstant(cd, true, false);
                if (result.STATUS == PARSE_STATUS.FAIL)
                    throw new SyntaxError("Number, Character or Constant", constantValue, cd.tokenizer);

                cd.constants.put(constantName, result.VALUE);
                return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, result.VALUE);
            }
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseNumber(@NotNull CompilerData cd, boolean addToProgram) {
        String numberToParse = cd.tokenizer.getLast();
        if (numberToParse == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        try {
            int value = StringUtils.parseInt(numberToParse);
            if (addToProgram) cd.program.add(value);
            return new ParseResult<>(addToProgram ? PARSE_STATUS.SUCCESS : PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, value);
        } catch (Exception err) {
            return new ParseResult<>(PARSE_STATUS.FAIL);
        }
    }

    private static ParseResult<Integer> parseRegister(CompilerData cd, boolean addToProgram) {
        String registerName = cd.tokenizer.getLast();
        if (registerName == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        int address;

        // We search for a Register with the specified name
        IRegister register = cd.processor.getRegister(registerName);
        if (register == null) {
            // If no register was found we search for a valid flag
            IFlag flag = cd.processor.getFlag(registerName);
            if (flag == null) return new ParseResult<>(PARSE_STATUS.FAIL);
            else if (flag instanceof MemoryFlag) {
                // If the flag is valid get its address
                address = ((MemoryFlag) flag).getAddress();
            } else throw new ProcessorError("Reading/Writing to Flag \"" + registerName + "\" isn't supported!", cd.tokenizer);
        } else if (register instanceof MemoryRegister) {
            // If the register is valid get its address
            address = ((MemoryRegister) register).getAddress();
        } else throw new ProcessorError("Reading/Writing to Register \"" + registerName + "\" isn't supported!", cd.tokenizer);

        if (!addToProgram) return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, registerName, address);

        cd.registers.put(cd.program.size(), registerName);
        cd.program.add(address);
        return new ParseResult<>(PARSE_STATUS.SUCCESS, registerName, address);
    }

    private static ParseResult<String> parseString(@NotNull CompilerData cd, boolean addToProgram) {
        String stringTerminator = cd.tokenizer.getLast();
        if (stringTerminator == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.STRING.matches(stringTerminator)) {
            boolean isEscapingCharacter = false;
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String currentToken = cd.tokenizer.consumeNext();
                if (currentToken == null)
                    throw new SyntaxError("String terminator ('" + stringTerminator + "')", String.valueOf(stringBuilder.charAt(stringBuilder.length() - 1)), cd.tokenizer);
                else if (isEscapingCharacter) {
                    char escapedCharacter = currentToken.charAt(0);
                    // If the character that is being escaped is a digit
                    if (Character.isDigit(escapedCharacter)) {
                        // Add the first digit
                        StringBuilder codePointBuilder = new StringBuilder()
                                .append(escapedCharacter);
                        int firstCharacterIndex;

                        // For each character in the current token
                        for (firstCharacterIndex = 1; firstCharacterIndex < currentToken.length(); firstCharacterIndex++) {
                            char currentChar = currentToken.charAt(firstCharacterIndex);
                            // If it's a digit add it to the codePointBuilder
                            if (Character.isDigit(currentChar))
                                codePointBuilder.append(currentChar);
                            // Else break, we've found all digits in the current token
                            else break;
                        }
                        // NOTE: firstCharacterIndex is now the index of the first character
                        //        which is not a digit in the current Token

                        int codePoint = -1;
                        boolean isValidCodePoint;
                        try {
                            // Parsing the codePoint that was found
                            codePoint = Integer.parseUnsignedInt(codePointBuilder.toString());
                            // Check if it's a valid code point
                            isValidCodePoint = Character.isValidCodePoint(codePoint);
                        } catch (Exception e) {
                            // It's also not a valid codepoint if Integer.parseUnsignedInt throws
                            //  (It can happen if the number is too big)
                            isValidCodePoint = false;
                        }
                        if (!isValidCodePoint) throw new SyntaxError("Valid Code Point", codePointBuilder.toString(), cd.tokenizer);
                        // Else append the codepoint and add the rest of the token to the final String
                        stringBuilder.appendCodePoint(codePoint);

                        // If the whole token was a CodePoint
                        if (firstCharacterIndex >= currentToken.length()) {
                            // Then if the next token is the one which terminates the CodePoint, consume it
                            if (Tokens.STR_CODEPOINT_TERMINATOR.matches(cd.tokenizer.peekNext()))
                                cd.tokenizer.consumeNext();
                        // Else append the rest of the string and don't consume the next token
                        } else stringBuilder.append(currentToken.substring(firstCharacterIndex));
                    } else if (StringUtils.SpecialCharacters.MAP.containsKey(escapedCharacter)) {
                        stringBuilder.append(StringUtils.SpecialCharacters.MAP.get(escapedCharacter));
                        if (currentToken.length() > 1)
                            stringBuilder.append(currentToken.substring(1));
                    } else stringBuilder.append(currentToken);
                    isEscapingCharacter = false;
                } else if (currentToken.equals(stringTerminator)) break;
                else if (Tokens.ESCAPE_CH.matches(currentToken)) isEscapingCharacter = true;
                else stringBuilder.append(currentToken);
            }

            if (addToProgram) {
                for (int i = 0; i < stringBuilder.length(); i++) cd.program.add((int) stringBuilder.charAt(i));
                return new ParseResult<>(PARSE_STATUS.SUCCESS, null, stringBuilder.toString());
            } else return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, stringBuilder.toString());
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseCharacter(@NotNull CompilerData cd, boolean addToProgram) {
        String charTerminator = cd.tokenizer.getLast();
        if (charTerminator == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.CHARACTER.matches(charTerminator)) {
            char character;

            String nextToken = cd.tokenizer.consumeNext();
            if (nextToken == null || charTerminator.equals(nextToken) || nextToken.length() > 1)
                throw new SyntaxError("Character", nextToken, cd.tokenizer);
            else if (Tokens.ESCAPE_CH.matches(nextToken)) {
                String escapedValue = cd.tokenizer.consumeNext();
                if (escapedValue == null)
                    throw new SyntaxError("Character or Code Point", null, cd.tokenizer);
                // If the char to add is a digit, then we have a code point
                else if (Character.isDigit(escapedValue.charAt(0))) {
                    StringBuilder codePointBuilder = new StringBuilder();
                    for (int i = 0; i < escapedValue.length(); i++) {
                        char currentChar = escapedValue.charAt(i);
                        // If it's not a digit it's not a valid Code Point
                        //  (Strings don't throw because they can have multiple
                        //    characters, so the code point can terminate mid-string)
                        if (!Character.isDigit(currentChar))
                            throw new SyntaxError("Code Point", escapedValue, cd.tokenizer);
                        codePointBuilder.append(currentChar);
                    }

                    int codePoint = -1;
                    boolean isValidCodePoint;
                    // Make sure that it's a valid integer and that's a valid code point
                    try {
                        codePoint = Integer.parseUnsignedInt(codePointBuilder.toString());
                        isValidCodePoint = Character.isValidCodePoint(codePoint);
                    } catch (Exception e) {
                        isValidCodePoint = false;
                    }

                    if (!isValidCodePoint)
                        throw new SyntaxError("Code Point", codePointBuilder.toString(), cd.tokenizer);

                    character = (char) codePoint;
                } else if (escapedValue.length() == 1)
                    character = StringUtils.SpecialCharacters.MAP.getOrDefault(escapedValue.charAt(0), escapedValue.charAt(0));
                else throw new SyntaxError("Character or Code Point", escapedValue, cd.tokenizer);
            } else character = nextToken.charAt(0);

            if (!charTerminator.equals(cd.tokenizer.consumeNext()))
                throw new SyntaxError("Character terminator ('" + charTerminator + "')", nextToken, cd.tokenizer);

            if (addToProgram) {
                cd.program.add((int) character);
                return new ParseResult<>(PARSE_STATUS.SUCCESS, null, (int) character);
            } else return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, (int) character);
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseValues(@NotNull CompilerData cd) {
        // Get the value to parse
        String valueToParse = cd.tokenizer.consumeNext(Tokens.SPACE);
        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new SyntaxError("Number, Char, Offset, Constant or Label", "null", cd.tokenizer);

        ParseResult<Integer> lastResult = parseNumber(cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseRegister(cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseCharacter(cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseOffset(cd, false, null, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(cd, true, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseLabel(cd, false);

        if (lastResult.STATUS == PARSE_STATUS.FAIL)
            throw new SyntaxError("Number, Char, Offset, Constant or Label", valueToParse, cd.tokenizer);
        return lastResult;
    }

    public static CompiledProgram compileFile(@NotNull File file, @NotNull IProcessor processor) {
        long compilationStartTimestamp = System.nanoTime();

        if (!file.exists()) throw new IllegalArgumentException("'" + file.getAbsolutePath() + "': The specified file doesn't exist.");
        if (!file.canRead()) throw new IllegalArgumentException("'" + file.getAbsolutePath() + "': The specified file can't be read.");

        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (Exception err) {
            throw new IllegalStateException("Something went wrong while reading the specified file.");
        }

        InstructionSet instructionSet = processor.getInstructionSet();

        // Using "cd" as the name to not use "compilerData" which
        //  would be much longer to write and may clutter lines
        CompilerData cd = new CompilerData(
                processor,
                new Tokenizer(fileContents, true, Tokens.ALL_TOKENS)
        );
        cd.tokenizer.removeEmpties();

        while (cd.tokenizer.hasNext()) {
            String tokenToParse = cd.tokenizer.consumeNext(Tokens.SPACE);
            if (tokenToParse == null) break;

            int instructionCode = instructionSet.getKeyCode(tokenToParse);
            Instruction instruction = instructionSet.getInstruction(instructionCode);

            if (instruction != null) {
                // If an instruction was found add it to the memory
                cd.program.add(instructionCode);
                for (int i = 0; i < instruction.ARGUMENTS;)
                    if (parseValues(cd).STATUS == PARSE_STATUS.SUCCESS) i++;

            } else if (Tokens.COMMENT.matches(tokenToParse)) {
                String comment;
                do {
                    comment = cd.tokenizer.consumeNext();
                } while (!Tokens.NEWLINE.matches(comment));
            } else if (Tokens.COMPILER.matches(tokenToParse)) {
                // Parsing Compiler Instructions
                String compilerInstr = cd.tokenizer.consumeNext(Tokens.SPACE);
                if (compilerInstr == null)
                    throw new SyntaxError("Compiler Instruction", "null", cd.tokenizer);
                else if (compilerInstr.equals(CI_DEFINE_WORD)) {
                    parseValues(cd);
                } else if (compilerInstr.equals(CI_DEFINE_STRING)) {
                    cd.tokenizer.consumeNext(Tokens.SPACE);
                    if (parseString(cd, true).STATUS == PARSE_STATUS.FAIL)
                        throw new SyntaxError("String", cd.tokenizer.getLast(), cd.tokenizer);
                } else if (compilerInstr.equals(CI_DEFINE_ARRAY)) {
                    // Be sure that there's the character that starts the array
                    String arrayStart = cd.tokenizer.consumeNext(Tokens.SPACE);
                    if (Tokens.ARR_START.matches(arrayStart)) {
                        while (true) {
                            // For each value in the array, check if the next value is the array closer character
                            String nextValue = cd.tokenizer.peekNext(Tokens.SPACE);
                            if (nextValue == null) {
                                throw new SyntaxError("Array terminator ('" + Tokens.ARR_END.getPattern() + "')", cd.tokenizer.getLast(), cd.tokenizer);
                            } else if (Tokens.ARR_END.matches(nextValue)) {
                                cd.tokenizer.consumeNext(Tokens.SPACE);
                                break;
                            }
                            parseValues(cd);
                        }
                    } else throw new SyntaxError("Array", arrayStart, cd.tokenizer);
                } else throw new SyntaxError("Compiler Instruction", compilerInstr, cd.tokenizer);
            } else {
                // Parsing Labels and Constants
                if (parseConstant(cd, false, true).STATUS == PARSE_STATUS.FAIL && parseLabel(cd, true).STATUS == PARSE_STATUS.FAIL)
                    throw new SyntaxError("Instruction, Constant or Label declaration", cd.tokenizer.getLast(), cd.tokenizer);
            }
        }

        // Processing Offsets and Labels
        cd.offsets.forEach((index, offset) -> cd.program.set(index, index + offset + cd.processor.getProgramAddress()));

        cd.labels.forEach((name, label) -> {
            if (label.getPointer() == OffsetLabel.NULL_PTR) throw new ReferenceError("Label", name, -1, -1);
            Integer[] instances = label.getInstances();
            for (Integer instance : instances)
                cd.program.set(instance, label.getPointerForInstance(instance) + cd.processor.getProgramAddress());
        });

        // Converting cd.program from Integer to int
        int[] primitiveIntProgram = new int[cd.program.size()];
        for (int i = 0; i < cd.program.size(); i++)
            primitiveIntProgram[i] = cd.program.get(i);

        return new CompiledProgram(processor, cd.labels, cd.registers, cd.offsets, primitiveIntProgram, System.nanoTime() - compilationStartTimestamp);
    }

    /**
     * Function that generates a pseudo-random String using the specified seed
     * @param seed The seed to generate the String from
     * @return A randomly generated String based on the specified seed
     */
    private static @NotNull String generateRandomString(int seed) {
        // Not allowing uppercase characters because Strings may be
        //  the same as Instruction names which should always be uppercase
        final char[] VALID_CHARS = new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };

        StringBuilder str = new StringBuilder();
        int num = seed;

        do {
            int remainder = num % VALID_CHARS.length;
            str.append(VALID_CHARS[remainder]);
            num /= VALID_CHARS.length;
        } while (num != 0);

        return str.toString();
    }

    private static @NotNull LabelData<BasicLabel> obfuscatePointers(@NotNull LabelData<OffsetLabel> labelData, @NotNull OffsetsData offsetsData) {
        LabelData<BasicLabel> obfLabels = new LabelData<>();

        AtomicInteger labelsCount = new AtomicInteger();
        labelData.forEach(
                (name, label) -> {
                    Integer[] labelInstances = label.getInstances();
                    if (labelInstances.length == 0) return;

                    HashMap<Integer, String> parsedOffsets = new HashMap<>();
                    for (Integer labelInstance : labelInstances) {
                        int offset = label.getOffsetForInstance(labelInstance);
                        String basicLabelName;
                        BasicLabel basicLabel;
                        if (parsedOffsets.containsKey(offset)) {
                            basicLabelName = parsedOffsets.get(offset);
                            basicLabel = obfLabels.get(basicLabelName);
                        } else {
                            basicLabelName = generateRandomString(labelsCount.getAndIncrement());
                            parsedOffsets.put(offset, basicLabelName);

                            basicLabel = new BasicLabel().setPointer(label.getPointer() + offset);
                            obfLabels.put(basicLabelName, basicLabel);
                        }
                        basicLabel.addInstance(labelInstance);
                    }
                }
        );

        offsetsData.forEach(
                (address, offset) -> {
                    String labelName = generateRandomString(labelsCount.getAndIncrement());
                    BasicLabel label = new BasicLabel().setPointer(address + offset).addInstance(address);
                    obfLabels.put(labelName, label);
                }
        );

        return obfLabels;
    }

    public static @NotNull String obfuscateProgram(@NotNull CompiledProgram compiledProgram) {
        int[] program = compiledProgram.getProgram();
        if (program.length == 0) return Tokens.COMMENT.getCharacter() + " Nothing to see here ;)";

        StringBuilder obfProgram = new StringBuilder();
        obfProgram.append(Tokens.COMPILER.getCharacter())
                  .append(CI_DEFINE_ARRAY)
                  .append(Tokens.SPACE.getCharacter())
                  .append(Tokens.ARR_START.getCharacter())
                  .append(Tokens.SPACE.getCharacter());

        // Holds all registers used in the program
        RegisterData programRegisters = compiledProgram.getRegisters();

        // Holds all labels used in the program
        LabelData<BasicLabel> obfLabels = obfuscatePointers( compiledProgram.getLabels(), compiledProgram.getOffsets() );

        // For each address of the program
        //  (it's <= because we also want to get any label that was declared at the end of the file)
        for (int currentAddress = 0; currentAddress <= program.length; currentAddress++) {
            // Check if there are labels declared at this address of the program
            if (obfLabels.hasLabelsAtAddress(currentAddress)) {
                // For each label declared at this address
                String[] labelNames = obfLabels.getLabelsAtAddress(currentAddress);
                for (String labelName : labelNames) {
                    // Add the label declaration to the program
                    obfProgram.append(labelName)
                              .append(Tokens.LABEL.getCharacter())
                              .append(Tokens.SPACE.getCharacter());
                }
            }

            // Meanwhile if we're putting data into the program there's none at
            //  program.length, so we only do this if it's < instead of <=
            if (currentAddress < program.length) {
                // If there's a register at the current program address add it
                if (programRegisters.hasRegisterAtAddress(currentAddress))
                    obfProgram.append( programRegisters.getRegisterAtAddress(currentAddress) );
                // If a label was used at the current program address
                else if (obfLabels.hasInstancesAtAddress(currentAddress)) {
                    // Get its name
                    String labelName = obfLabels.getInstancesAtAddress(currentAddress);

                    // Add the label to the program
                    obfProgram.append(labelName);
                // If none of the above then just add the number to the program
                } else obfProgram.append( program[currentAddress] );
            }

            // Add a space character if there's none at the end
            if (obfProgram.charAt(obfProgram.length() - 1) != Tokens.SPACE.getCharacter())
                obfProgram.append(Tokens.SPACE.getCharacter());
        }

        obfProgram.append(Tokens.ARR_END.getCharacter());
        return obfProgram.toString();
    }
}
