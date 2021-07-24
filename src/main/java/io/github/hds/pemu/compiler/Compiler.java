package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.labels.BasicLabel;
import io.github.hds.pemu.compiler.labels.OffsetLabel;
import io.github.hds.pemu.instructions.Instruction;
import io.github.hds.pemu.instructions.InstructionSet;
import io.github.hds.pemu.memory.flags.IFlag;
import io.github.hds.pemu.memory.flags.IMemoryFlag;
import io.github.hds.pemu.memory.registers.IMemoryRegister;
import io.github.hds.pemu.memory.registers.IRegister;
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
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/* Adding this comment just to let people know that this thing
 * is pretty sketchy, though I've never done something
 * like this so it was to be expected
 */
public final class Compiler {

    protected static final String CI_DEFINE_WORD   = "DW";
    protected static final String CI_DEFINE_STRING = "DS";
    protected static final String CI_DEFINE_ARRAY  = "DA";
    protected static final String CI_INCLUDE       = "INCLUDE";

    protected static class Tokens {

        public static final Token COMMENT   = new Token(';');
        public static final Token CONSTANT  = new Token('@');
        public static final Token LABEL     = new Token(':');
        public static final Token COMPILER  = new Token('#');
        public static final Token STRING    = new Token('"', "[\"']", false);
        public static final Token CHARACTER = new Token('\'');
        public static final Token ESCAPE_CH = new Token('\\', true);
        public static final Token ARR_START = new Token('{', true);
        public static final Token ARR_END   = new Token('}', true);
        public static final Token ARR_SIZE_START = new Token('[', true);
        public static final Token ARR_SIZE_END   = new Token(']', true);
        public static final Token OFF_START = new Token('[', true);
        public static final Token OFF_END   = new Token(']', true);
        public static final Token SPACE     = new Token(' ', "\\h+", false);
        public static final Token NEWLINE   = new Token('\n', "\\v+", false);
        public static final Token STR_CODEPOINT_TERMINATOR = new Token(';');

        // This Token isn't part of the Tokenizer so that Vertical and Horizontal Spaces are separated
        // This is useful when you want to ignore/accept all types of Spaces
        public static final Token WHITESPACE = new Token(' ', "\\s+", false);

        // The class TokenGroup makes sure that no duplicate pattern is present,
        //  so it discards a Token if one that is equal is present, this should make Tokenizer a bit faster
        public static final TokenGroup ALL_TOKENS = new TokenGroup().addTokens(
                COMMENT, CONSTANT, LABEL, COMPILER, STRING, CHARACTER, ESCAPE_CH,
                ARR_START, ARR_END, ARR_SIZE_START, ARR_SIZE_END, OFF_START, OFF_END,
                SPACE, NEWLINE, STR_CODEPOINT_TERMINATOR
        );

    }

    protected static class CompilerData {
        public final @NotNull IProcessor processor;
        public final @NotNull ArrayList<Integer> program;
        public final @NotNull LabelData<OffsetLabel> labels;
        public final @NotNull HashMap<String, Constant> constants;
        public final @NotNull RegisterData registers;
        public final @NotNull OffsetsData offsets;

        protected CompilerData(@NotNull IProcessor processor) {
            this.processor = processor;
            this.program = new ArrayList<>();
            this.labels = new LabelData<>();
            this.constants = Constants.getDefaultConstants();
            this.registers = new RegisterData();
            this.offsets = new OffsetsData();
        }
    }

    public static class CompilerError extends RuntimeException {
        protected CompilerError(@Nullable File file, @NotNull String errorName, @NotNull String message, @Nullable Tokenizer tokenizer) {
            this(
                    file, errorName, message,
                    tokenizer == null ? -1 : (tokenizer.getConsumedLines() + 1), // Adding 1 because the current line isn't consumed yet
                    tokenizer == null ? -1 : tokenizer.getConsumedLineCharacters()
            );
        }

        protected CompilerError(@Nullable File file, @NotNull String errorName, @NotNull String message, int errorLine, int errorChar) {
            super(
                    String.format(
                            "'%s': %s Error (%d:%d): %s.",
                            file == null ? "Unknown" : file.getName(), errorName, errorLine, errorChar, message
                    )
            );
        }
    }

    public static class SyntaxError extends CompilerError {
        protected SyntaxError(@Nullable File file, @NotNull String expected, @Nullable String got, @Nullable Tokenizer tokenizer) {
            this(file, expected, got, false, tokenizer);
        }

        protected SyntaxError(@Nullable File file, @NotNull String expected, @Nullable String got, boolean noCharEscape, @Nullable Tokenizer tokenizer) {
            super(
                    file, "Syntax",
                    String.format(
                            "Expected %s, got '%s'",
                            expected, noCharEscape ? got : StringUtils.SpecialCharacters.escapeAll(String.valueOf(got))
                    ), tokenizer
            );
        }
    }

    public static class ReferenceError extends CompilerError {
        protected ReferenceError(@Nullable File file, @NotNull String type, @NotNull String name, @NotNull String description, @Nullable Tokenizer tokenizer) {
            super(
                    file, "Reference",
                    String.format("%s '%s' %s", type, name, description),
                    tokenizer
            );
        }

        protected ReferenceError(@Nullable File file, @NotNull String type, @NotNull String name, @NotNull String description, int errorLine, int errorChar) {
            super(
                    file, "Reference",
                    String.format("%s '%s' %s", type, name, description),
                    errorLine, errorChar
            );
        }
    }

    public static class TypeError extends CompilerError {
        protected TypeError(@Nullable File file, @NotNull String message, @Nullable Tokenizer tokenizer) {
            super(file, "Type", message, tokenizer);
        }
    }

    public static class FileError extends CompilerError {
        protected FileError(@NotNull File file, @NotNull String message, @Nullable Tokenizer tokenizer) {
            super(file, "File", message, tokenizer);
        }
    }

    public static class ProcessorError extends CompilerError {
        protected ProcessorError(@Nullable File file, @NotNull String message, @Nullable Tokenizer tokenizer) {
            super(file, "Processor", message, tokenizer);
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

    private static ParseResult<Integer> parseOffset(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean peekNext, @Nullable Token peekBlacklist, boolean addToProgram) {

        String offsetStart = peekNext ? (peekBlacklist == null ? tokenizer.peekNext() : tokenizer.peekNext(peekBlacklist)) : tokenizer.getLast();
        if (offsetStart == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.OFF_START.matches(offsetStart)) {
            if (peekNext) tokenizer.consumeNext(Tokens.WHITESPACE);
            String offsetToParse = tokenizer.consumeNext(Tokens.WHITESPACE);

            ParseResult<Integer> lastResult = parseNumber(tokenizer, file, cd, false);
            if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(tokenizer, file, cd, true, false);
            if (lastResult.STATUS == PARSE_STATUS.FAIL) throw new SyntaxError(file, "Constant or Number", offsetToParse, tokenizer);

            int offset = lastResult.VALUE;

            String offsetEnd = tokenizer.consumeNext(Tokens.WHITESPACE);
            if (Tokens.OFF_END.matches(offsetEnd)) {
                if (addToProgram) {
                    cd.offsets.put(cd.program.size(), offset);
                    cd.program.add(0);
                    return new ParseResult<>(PARSE_STATUS.SUCCESS, null, offset);
                } else return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, offset);
            } else throw new SyntaxError(file, "Offset terminator ('" + Tokens.OFF_END.getCharacter() + "')", offsetEnd, tokenizer);
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseLabel(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean canDeclare, boolean canUse) {
        String labelName = tokenizer.getLast();
        if (labelName == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        boolean isBeingDeclared = Tokens.LABEL.matches(
                tokenizer.peekNext(Tokens.WHITESPACE)
        );

        if (isBeingDeclared) {
            if (!canDeclare) throw new TypeError(file, "Label Declaration is not allowed here", tokenizer);

            // If a label is being declared consume the declaration token
            tokenizer.consumeNext(Tokens.WHITESPACE);
            if (cd.labels.containsKey(labelName)) {
                // If a label was already created check if it has a pointer
                OffsetLabel label = cd.labels.get(labelName);
                if (label.hasPointer())
                    // If the label has a valid pointer then it was already declared!
                    throw new TypeError(file, "Label '" + labelName + "' was already declared", tokenizer);
                else label.setPointer(cd.program.size());
            } else
                // If no label was created then create it
                cd.labels.put(labelName, new OffsetLabel().setPointer(cd.program.size()));
            return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, labelName, cd.program.size());
        } else if (canUse) {
            int offset = 0;
            ParseResult<Integer> offsetResult = parseOffset(tokenizer, file, cd, true, null, false);
            if (offsetResult.STATUS != PARSE_STATUS.FAIL) offset = offsetResult.VALUE;

            OffsetLabel label;
            if (cd.labels.containsKey(labelName)) {
                label = cd.labels.get(labelName);
            } else {
                label = new OffsetLabel();
                cd.labels.put(labelName, label);
            }

            label.addInstance(cd.program.size(), offset);
            // Only set instance location if it's the first one
            if (label.getInstancesCount() == 1)
                label.setInstanceLocation(file, tokenizer.getConsumedLines() + 1, tokenizer.getConsumedLineCharacters());
            cd.program.add(0);
            return new ParseResult<>(PARSE_STATUS.SUCCESS, labelName, cd.program.size());
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseConstant(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean isGetting, boolean addToProgram) {
        String constantPrefix = tokenizer.getLast();
        if (constantPrefix == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.CONSTANT.matches(constantPrefix)) {
            String constantName = tokenizer.consumeNext(Tokens.WHITESPACE);
            if (constantName == null) throw new SyntaxError(file, "Constant's name", "null", tokenizer);
            if (isGetting) {
                if (cd.constants.containsKey(constantName)) {
                    Constant constant = cd.constants.get(constantName);

                    if (addToProgram) {
                        constant.addInstance(cd.program.size());
                        cd.program.add(0);
                        return new ParseResult<>(PARSE_STATUS.SUCCESS, constantName, 0);
                    }

                    return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, getConstantValue(constant, tokenizer, file));
                } else throw new ReferenceError(file, "Constant", constantName, "was not declared", tokenizer);
            } else {
                String constantValue = tokenizer.consumeNext(Tokens.WHITESPACE);
                if (constantValue == null) throw new SyntaxError(file, "Number, Character or Constant", "null", tokenizer);

                boolean isReference = false;
                ParseResult<Integer> result = parseNumber(tokenizer, file, cd, false);

                if (result.STATUS == PARSE_STATUS.FAIL) result = parseCharacter(tokenizer, file, cd, false);

                if (result.STATUS == PARSE_STATUS.FAIL) {
                    result = parseConstant(tokenizer, file, cd, true, false);
                    isReference = true;
                }

                if (result.STATUS == PARSE_STATUS.FAIL)
                    throw new SyntaxError(file, "Number, Character or Constant", constantValue, tokenizer);

                Constant constant;
                if (cd.constants.containsKey(constantName))
                    constant = cd.constants.get(constantName);
                else {
                    constant = new Constant(constantName, result.VALUE);
                    cd.constants.put(constantName, constant);
                }

                if (isReference) {
                    constant.setReference(cd.constants.get(result.NAME));

                    ArrayList<String> references = new ArrayList<>();
                    if (constant.isCircularReference(references)) {
                        throw new ReferenceError(
                                file, "Constant",
                                Constant.formatReferences(String.valueOf(Tokens.CONSTANT.getCharacter()), references),
                                "is Circular Reference", tokenizer
                        );
                    }
                } else constant.setValue(result.VALUE);

                return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, constantName, result.VALUE);
            }
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseNumber(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean addToProgram) {
        String numberToParse = tokenizer.getLast();
        if (numberToParse == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        try {
            int value = StringUtils.parseInt(numberToParse);
            if (addToProgram) cd.program.add(value);
            return new ParseResult<>(addToProgram ? PARSE_STATUS.SUCCESS : PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, value);
        } catch (Exception err) {
            return new ParseResult<>(PARSE_STATUS.FAIL);
        }
    }

    private static ParseResult<Integer> parseRegister(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean addToProgram) {
        String registerName = tokenizer.getLast();
        if (registerName == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        int address;

        // We search for a Register with the specified name
        IRegister register = cd.processor.getRegister(registerName);
        if (register == null) {
            // If no register was found we search for a valid flag
            IFlag flag = cd.processor.getFlag(registerName);
            if (flag == null) return new ParseResult<>(PARSE_STATUS.FAIL);
            else if (flag instanceof IMemoryFlag) {
                // If the flag is valid get its address
                address = ((IMemoryFlag) flag).getAddress();
            } else throw new ProcessorError(file, "Reading/Writing to Flag \"" + registerName + "\" isn't supported!", tokenizer);
        } else if (register instanceof IMemoryRegister) {
            // If the register is valid get its address
            address = ((IMemoryRegister) register).getAddress();
        } else throw new ProcessorError(file, "Reading/Writing to Register \"" + registerName + "\" isn't supported!", tokenizer);

        if (!addToProgram) return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, registerName, address);

        cd.registers.put(cd.program.size(), registerName);
        cd.program.add(address);
        return new ParseResult<>(PARSE_STATUS.SUCCESS, registerName, address);
    }

    private static ParseResult<String> parseString(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean addToProgram) {
        String stringTerminator = tokenizer.getLast();
        if (stringTerminator == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.STRING.matches(stringTerminator)) {
            boolean isEscapingCharacter = false;
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String currentToken = tokenizer.consumeNext();
                if (currentToken == null)
                    throw new SyntaxError(file, "String terminator ('" + stringTerminator + "')", String.valueOf(stringBuilder.charAt(stringBuilder.length() - 1)), tokenizer);
                else if (Tokens.NEWLINE.matches(currentToken))
                    throw new SyntaxError(file, "String character", "New Line", tokenizer);
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
                        if (!isValidCodePoint) throw new SyntaxError(file, "Valid Code Point", codePointBuilder.toString(), tokenizer);
                        // Else append the codepoint and add the rest of the token to the final String
                        stringBuilder.appendCodePoint(codePoint);

                        // If the whole token was a CodePoint
                        if (firstCharacterIndex >= currentToken.length()) {
                            // Then if the next token is the one which terminates the CodePoint, consume it
                            if (Tokens.STR_CODEPOINT_TERMINATOR.matches(tokenizer.peekNext()))
                                tokenizer.consumeNext();
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

    private static ParseResult<Integer> parseCharacter(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean addToProgram) {
        String charTerminator = tokenizer.getLast();
        if (charTerminator == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.CHARACTER.matches(charTerminator)) {
            char character;

            String nextToken = tokenizer.consumeNext();
            if (Tokens.NEWLINE.matches(nextToken))
                throw new SyntaxError(file, "Character", "New Line", tokenizer);
            else if (nextToken == null || charTerminator.equals(nextToken) || nextToken.length() > 1)
                throw new SyntaxError(file, "Character", nextToken, tokenizer);
            else if (Tokens.ESCAPE_CH.matches(nextToken)) {
                String escapedValue = tokenizer.consumeNext();
                if (escapedValue == null)
                    throw new SyntaxError(file, "Character or Code Point", null, tokenizer);
                // If the char to add is a digit, then we have a code point
                else if (Character.isDigit(escapedValue.charAt(0))) {
                    StringBuilder codePointBuilder = new StringBuilder();
                    for (int i = 0; i < escapedValue.length(); i++) {
                        char currentChar = escapedValue.charAt(i);
                        // If it's not a digit it's not a valid Code Point
                        //  (Strings don't throw because they can have multiple
                        //    characters, so the code point can terminate mid-string)
                        if (!Character.isDigit(currentChar))
                            throw new SyntaxError(file, "Code Point", escapedValue, tokenizer);
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
                        throw new SyntaxError(file, "Code Point", codePointBuilder.toString(), tokenizer);

                    character = (char) codePoint;
                } else if (escapedValue.length() == 1)
                    character = StringUtils.SpecialCharacters.MAP.getOrDefault(escapedValue.charAt(0), escapedValue.charAt(0));
                else throw new SyntaxError(file, "Character or Code Point", escapedValue, tokenizer);
            } else character = nextToken.charAt(0);

            if (!charTerminator.equals(tokenizer.consumeNext()))
                throw new SyntaxError(file, "Character terminator ('" + charTerminator + "')", nextToken, tokenizer);

            if (addToProgram) {
                cd.program.add((int) character);
                return new ParseResult<>(PARSE_STATUS.SUCCESS, null, (int) character);
            } else return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED, null, (int) character);
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Void> parseComment(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd, boolean peekNext) {
        String commentToken = peekNext ? tokenizer.peekNext(Tokens.WHITESPACE) : tokenizer.getLast();
        if (commentToken == null) return new ParseResult<>(PARSE_STATUS.FAIL);

        if (Tokens.COMMENT.matches(commentToken)) {
            tokenizer.consumeNext(true, Tokens.NEWLINE);
            return new ParseResult<>(PARSE_STATUS.SUCCESS_PROGRAM_NOT_CHANGED);
        } else return new ParseResult<>(PARSE_STATUS.FAIL);
    }

    private static ParseResult<Integer> parseValues(@NotNull Tokenizer tokenizer, @NotNull File file, @NotNull CompilerData cd) {
        // Get the value to parse
        String valueToParse = tokenizer.consumeNext(Tokens.WHITESPACE);
        // Throw if there's no value, because there MUST be one if this function is called
        if (valueToParse == null) throw new SyntaxError(file, "Number, Char, Offset, Constant or Label", "null", tokenizer);

        ParseResult<Integer> lastResult = parseNumber(tokenizer, file, cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseRegister(tokenizer, file, cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseCharacter(tokenizer, file, cd, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseOffset(tokenizer, file, cd, false, null, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseConstant(tokenizer, file, cd, true, true);
        if (lastResult.STATUS == PARSE_STATUS.FAIL) lastResult = parseLabel(tokenizer, file, cd, true, true);

        if (lastResult.STATUS == PARSE_STATUS.FAIL)
            throw new SyntaxError(file, "Number, Char, Offset, Constant or Label", valueToParse, tokenizer);
        return lastResult;
    }

    public static int getConstantValue(@NotNull Constant constant, @Nullable Tokenizer tokenizer, @Nullable File file) {
        ArrayList<String> constantReferences = new ArrayList<>();
        int value;
        try {
            value = constant.getValue(constantReferences);
        } catch (Exception err) {
            throw new ReferenceError(
                    file, "Constant",
                    Constant.formatReferences(String.valueOf(Tokens.CONSTANT.getCharacter()), constantReferences),
                    "is Circular Reference", tokenizer
            );
        }
        return value;
    }

    public static @NotNull CompiledProgram compileFile(@NotNull File file, @NotNull IProcessor processor) {
        long compilationStartTimestamp = System.nanoTime();

        if (!file.exists())
            throw new FileError(file, "Couldn't compile file because it doesn't exist", null);
        if (!file.canRead())
            throw new FileError(file, "Couldn't compile file because it can't be read", null);

        // Using "cd" as the name to not use "compilerData" which
        //  would be much longer to write and may clutter lines
        CompilerData cd = new CompilerData(processor);
        internalCompileFile(file, new HashSet<>(), cd);

        // Processing Constants, Offsets and Labels
        cd.constants.forEach((name, constant) -> {
            int constantValue = getConstantValue(constant, null, null);
            for (Integer instance : constant.getInstances())
                cd.program.set(instance, constantValue);
        });

        cd.offsets.forEach((index, offset) -> cd.program.set(index, index + offset + cd.processor.getProgramAddress()));

        cd.labels.forEach((name, label) -> {
            if (!label.hasPointer())
                throw new ReferenceError(
                        label.getInstanceFile(), "Label", name, "was not declared",
                        label.getInstanceLine(), label.getInstanceChar()
                );
            Integer[] instances = label.getInstances();
            for (Integer instance : instances)
                cd.program.set(instance, label.getPointerForInstance(instance) + cd.processor.getProgramAddress());
        });

        // Converting cd.program from Integer to int
        int[] primitiveIntProgram = new int[cd.program.size()];
        for (int i = 0; i < cd.program.size(); i++)
            primitiveIntProgram[i] = cd.program.get(i);

        return new CompiledProgram(
                processor, cd.labels, cd.registers, cd.offsets, primitiveIntProgram, System.nanoTime() - compilationStartTimestamp
        );
    }

    private static void internalCompileFile(@NotNull File file, @NotNull HashSet<String> compiledFiles, @NotNull CompilerData cd) {
        String filePath;
        try {
            // Getting the Canonical Path, if it fails then get the Absolute one
            filePath = file.getCanonicalPath();
        } catch (Exception err) {
            filePath = file.getAbsolutePath();
        }

        // Make sure that we're not compiling an already compiled file
        if (compiledFiles.contains(filePath))
            return;
        compiledFiles.add(filePath);

        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (Exception err) {
            throw new FileError(file, "Something went wrong when reading file", null);
        }

        Tokenizer tokenizer = new Tokenizer(fileContents, Tokens.ALL_TOKENS);
        InstructionSet instructionSet = cd.processor.getInstructionSet();

        while (tokenizer.hasNext()) {
            String tokenToParse = tokenizer.consumeNext(Tokens.WHITESPACE);
            if (tokenToParse == null) break;

            int instructionCode = instructionSet.getKeyCode(tokenToParse);
            Instruction instruction = instructionSet.getInstruction(instructionCode);

            if (instruction != null) {
                // If an instruction was found add it to the memory
                cd.program.add(instructionCode);
                for (int i = 0; i < instruction.getArgumentsCount();)
                    // We go to the next argument ONLY if something was added to the program
                    if (parseValues(tokenizer, file, cd).STATUS == PARSE_STATUS.SUCCESS) i++;
            } else if (Tokens.COMPILER.matches(tokenToParse)) {
                // Parsing Compiler Instructions
                String compilerInstruction = tokenizer.consumeNext();
                if (compilerInstruction == null)
                    throw new SyntaxError(file, "Compiler Instruction", "null", tokenizer);
                else if (compilerInstruction.equals(CI_DEFINE_WORD)) {
                    parseValues(tokenizer, file, cd);
                } else if (compilerInstruction.equals(CI_DEFINE_STRING)) {
                    tokenizer.consumeNext(Tokens.WHITESPACE);
                    if (parseString(tokenizer, file, cd, true).STATUS == PARSE_STATUS.FAIL)
                        throw new SyntaxError(file, "String", tokenizer.getLast(), tokenizer);
                } else if (compilerInstruction.equals(CI_DEFINE_ARRAY)) {
                    // Be sure that there's the character that starts the array
                    String arrayStart = tokenizer.consumeNext(Tokens.WHITESPACE);
                    if (Tokens.ARR_START.matches(arrayStart)) {
                        while (true) {
                            // For each value in the array, check if the next value is the array closer character
                            String nextValue = tokenizer.peekNext(Tokens.WHITESPACE);
                            if (nextValue == null) {
                                throw new SyntaxError(file, "Array terminator ('" + Tokens.ARR_END.getCharacter() + "')", tokenizer.getLast(), tokenizer);
                            } else if (Tokens.ARR_END.matches(nextValue)) {
                                tokenizer.consumeNext(Tokens.WHITESPACE);
                                break;
                            }

                            // If we fail to parse a comment then it must be a Value
                            if (parseComment(tokenizer, file, cd, true).STATUS == PARSE_STATUS.FAIL) parseValues(tokenizer, file, cd);
                        }
                    } else if (Tokens.ARR_SIZE_START.matches(arrayStart)) {
                        tokenizer.consumeNext(Tokens.WHITESPACE);
                        ParseResult<Integer> result = parseNumber(tokenizer, file, cd, false);
                        if (result.STATUS == PARSE_STATUS.FAIL) result = parseConstant(tokenizer, file, cd, true, false);
                        if (result.STATUS == PARSE_STATUS.FAIL)
                            throw new SyntaxError(file, "Number or Constant", tokenizer.getLast(), tokenizer);

                        if (result.VALUE < 0)
                            throw new SyntaxError(file, "Valid Array Size (>= 0)", result.VALUE.toString(), tokenizer);

                        String arraySizeTerminator = tokenizer.peekNext(Tokens.WHITESPACE);
                        if (Tokens.ARR_SIZE_END.matches(arraySizeTerminator))
                            tokenizer.consumeNext(Tokens.WHITESPACE);
                        else
                            throw new SyntaxError(
                                    file, "Array Size terminator ('" + Tokens.ARR_SIZE_END.getCharacter() + "')",
                                    arraySizeTerminator == null ? tokenizer.getLast() : arraySizeTerminator, tokenizer
                            );

                        for (int i = 0; i < result.VALUE; i++) cd.program.add(0);
                    } else throw new SyntaxError(file, "Array or Array Size", arrayStart, tokenizer);
                } else if (compilerInstruction.equals(CI_INCLUDE)) {
                    tokenizer.consumeNext(Tokens.WHITESPACE);
                    ParseResult<String> parsedPath = parseString(tokenizer, file, cd, false);
                    if (parsedPath.STATUS == PARSE_STATUS.FAIL)
                        throw new SyntaxError(file, "Path (String)", tokenizer.getLast(), tokenizer);

                    String includePath = parsedPath.VALUE;
                    File includeFile;
                    try {
                        includeFile = new File(file.toURI().resolve(includePath));
                    } catch (Exception err) {
                        throw new SyntaxError(file, "Valid Include Path", includePath, tokenizer);
                    }

                    if (!includeFile.exists())
                        throw new FileError(file, "Couldn't include file because it doesn't exist", tokenizer);
                    if (!includeFile.canRead())
                        throw new FileError(file, "Couldn't include file because it can't be read", tokenizer);
                    internalCompileFile(includeFile, compiledFiles, cd);

                } else throw new SyntaxError(file, "Compiler Instruction", compilerInstruction, tokenizer);
            } else {
                // Parsing Comments, Labels and Constants
                if (
                        parseComment(tokenizer, file, cd, false).STATUS == PARSE_STATUS.FAIL &&
                        parseConstant(tokenizer, file, cd, false, true).STATUS == PARSE_STATUS.FAIL &&
                        parseLabel(tokenizer, file, cd, true, false).STATUS == PARSE_STATUS.FAIL
                ) throw new SyntaxError(file, "Instruction, Constant or Label declaration", tokenizer.getLast(), tokenizer);
            }
        }
    }

    private static final char[] RANDOM_STRING_CHARACTERS;
    static {
        // Not allowing uppercase characters because Strings may be
        //  the same as Instruction names which should always be uppercase
        RANDOM_STRING_CHARACTERS = new char['z' - 'a' + 1];
        for (int i = 0; i < RANDOM_STRING_CHARACTERS.length; i++) {
            RANDOM_STRING_CHARACTERS[i] = (char) ('a' + i);
        }
    }

    /**
     * Function that generates a pseudo-random String using the specified seed
     * @param seed The seed to generate the String from
     * @return A pseudo-randomly generated String based on the specified seed
     */
    private static @NotNull String generateRandomString(int seed) {
        // The fun thing about this whole function...
        // Is that it's basically a Decimal to whatever base converter
        //  but the output String is reversed
        StringBuilder str = new StringBuilder();

        do {
            int remainder = seed % RANDOM_STRING_CHARACTERS.length;
            str.append(RANDOM_STRING_CHARACTERS[remainder]);
            seed /= RANDOM_STRING_CHARACTERS.length;
        } while (seed != 0);

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

                            // Not moving debugging info (like Label Instance Location) because the given
                            //  data should have already compiled successfully once
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
                  .append(Tokens.WHITESPACE.getCharacter())
                  .append(Tokens.ARR_START.getCharacter())
                  .append(Tokens.WHITESPACE.getCharacter());

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
                              .append(Tokens.WHITESPACE.getCharacter());
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
            if (obfProgram.charAt(obfProgram.length() - 1) != Tokens.WHITESPACE.getCharacter())
                obfProgram.append(Tokens.WHITESPACE.getCharacter());
        }

        obfProgram.append(Tokens.ARR_END.getCharacter());
        return obfProgram.toString();
    }
}
