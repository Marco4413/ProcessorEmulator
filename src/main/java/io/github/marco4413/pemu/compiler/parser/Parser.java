package io.github.marco4413.pemu.compiler.parser;

import io.github.marco4413.pemu.compiler.CompilerVars;
import io.github.marco4413.pemu.files.FileUtils;
import io.github.marco4413.pemu.instructions.Instruction;
import io.github.marco4413.pemu.instructions.InstructionSet;
import io.github.marco4413.pemu.utils.IDoubleSupplier;
import io.github.marco4413.pemu.math.parser.MathParser;
import io.github.marco4413.pemu.memory.flags.IFlag;
import io.github.marco4413.pemu.memory.flags.IMemoryFlag;
import io.github.marco4413.pemu.memory.registers.IMemoryRegister;
import io.github.marco4413.pemu.memory.registers.IRegister;
import io.github.marco4413.pemu.processor.IProcessor;
import io.github.marco4413.pemu.tokenizer.Token;
import io.github.marco4413.pemu.tokenizer.TokenDefinition;
import io.github.marco4413.pemu.tokenizer.Tokenizer;
import io.github.marco4413.pemu.utils.IPIntSupplier;
import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public final class Parser {
    private static final char ESCAPE_CHAR = '\\';
    private static final char ESCAPE_TERM = ';';

    private static final String BASIC_TYPES = "Number, Math Expression, Character or Compiler Variable";
    private static final String ARGUMENT_TYPES = "Label, Offset, Register, " + BASIC_TYPES;
    private static final String GENERIC_TYPES = "Instruction, Compiler Instruction, Compiler Variable or Label";

    private static final String C_INSTR_DEFINE_WORD   = "DW";
    private static final String C_INSTR_DEFINE_STRING = "DS";
    private static final String C_INSTR_DEFINE_ARRAY  = "DA";
    private static final String C_INSTR_INCLUDE = "INCLUDE";
    private static final String[] ALL_C_INSTR = new String[] {
            C_INSTR_DEFINE_WORD, C_INSTR_DEFINE_STRING, C_INSTR_DEFINE_ARRAY, C_INSTR_INCLUDE
    };

    private static final String NUMBER_DIGIT_SEP = "_";
    private static final String GENERIC_NUMBER_PATTERN =
            StringUtils.format("{1}[{0}]+(?:(?:{3})*[{0}]+)*{2}", "{0}", "{1}", "{2}", Pattern.quote(NUMBER_DIGIT_SEP));

    private static final TokenDefinition COMMENT    = new TokenDefinition("Comment", ";[^\\v]*\\v?");
    private static final TokenDefinition LABEL_DECL = new TokenDefinition("Label Declaration", ":", true);
    private static final TokenDefinition HEX_NUMBER = new TokenDefinition("Hex Number", StringUtils.format(GENERIC_NUMBER_PATTERN, "0-9A-F", "0x", ""), false, true);
    private static final TokenDefinition OCT_NUMBER = new TokenDefinition("Octal Number", StringUtils.format(GENERIC_NUMBER_PATTERN, "0-7", "0o", ""));
    private static final TokenDefinition BIN_NUMBER = new TokenDefinition("Binary Number", StringUtils.format(GENERIC_NUMBER_PATTERN, "01", "0b", ""));
    private static final TokenDefinition DEC_NUMBER = new TokenDefinition("Decimal Number", StringUtils.format(GENERIC_NUMBER_PATTERN, "0-9", "[+\\-]?", ""));
    private static final TokenDefinition STRING     = new TokenDefinition("String", "\"((?:[^\\\\\"]|(?:\\\\[0-9]+;?|\\\\.))*)\"");
    private static final TokenDefinition CHARACTER  = new TokenDefinition("Character", "'(\\\\.|\\\\[0-9]+;?|[^\\\\])'");
    private static final TokenDefinition C_VAR      = new TokenDefinition("Compiler Variable", "@([_A-Z][_A-Z0-9]*)", false, true);
    private static final TokenDefinition C_INSTR    = new TokenDefinition("Compiler Instruction", StringUtils.format("#({0})", String.join("|", ALL_C_INSTR)));
    private static final TokenDefinition L_BRACE    = new TokenDefinition("Left Brace", "{", true);
    private static final TokenDefinition R_BRACE    = new TokenDefinition("Right Brace", "}", true);
    private static final TokenDefinition L_BRACKET  = new TokenDefinition("Left Bracket", "[", true);
    private static final TokenDefinition R_BRACKET  = new TokenDefinition("Right Bracket", "]", true);
    private static final TokenDefinition LABEL_OFF  = new TokenDefinition("Label with Offset", "([_A-Z][_A-Z0-9]*)\\[", false, true);
    private static final TokenDefinition MATH_EXPR  = new TokenDefinition("Math Expression", "(%\\{)([^}]*)}");
    private static final TokenDefinition IDENTIFIER = new TokenDefinition("Identifier", "[_A-Z][_A-Z0-9]*", false, true);
    private static final TokenDefinition SPACE      = new TokenDefinition("Space", "\\h+");
    private static final TokenDefinition NEWLINE    = new TokenDefinition("New Line", "\\v+");

    private static final TokenDefinition[] ALL_DEFINITIONS = new TokenDefinition[] {
            LABEL_DECL, COMMENT,
            HEX_NUMBER, OCT_NUMBER, BIN_NUMBER, DEC_NUMBER,
            STRING, CHARACTER,
            C_VAR, C_INSTR,
            L_BRACE, R_BRACE,
            L_BRACKET, R_BRACKET,
            LABEL_OFF, MATH_EXPR, IDENTIFIER,
            SPACE, NEWLINE
    };

    private static @NotNull String formatToken(@Nullable Token token) {
        return token == null ? "EOF" : token.getMatch();
    }

    private static class ParseResult<T> {
        public final boolean SUCCESS;
        public final T VALUE;

        protected ParseResult() {
            this(false, null);
        }

        protected ParseResult(boolean success, T value) {
            SUCCESS = success;
            VALUE = value;
        }
    }

    private static @NotNull ParseResult<IPIntSupplier> parseNumber(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();

        int value;
        if (
                HEX_NUMBER.isDefinitionOf(currentToken) ||
                OCT_NUMBER.isDefinitionOf(currentToken) ||
                BIN_NUMBER.isDefinitionOf(currentToken) ||
                DEC_NUMBER.isDefinitionOf(currentToken)
        ) {
            assert currentToken != null;
            value = StringUtils.parseInt(
                    currentToken.getMatch().replace(NUMBER_DIGIT_SEP, "")
            );
        } else return new ParseResult<>();

        IPIntSupplier valueSupplier = data -> value;
        if (addNodes) ctx.addNode(new ValueNode(valueSupplier));

        return new ParseResult<>(true, valueSupplier);
    }

    private static @NotNull ParseResult<IPIntSupplier> parseMathExpr(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();

        IPIntSupplier valueSupplier;
        if (MATH_EXPR.isDefinitionOf(currentToken)) {
            assert currentToken != null;
            String[] tokenGroups = currentToken.getGroups();

            IDoubleSupplier mathSupplier = MathParser.parseMath(
                    tokenGroups[1],
                    C_VAR, (token, data) -> {
                        // Getting var name from groups ( '@(.*)', there's only 1 group which has the name )
                        String varName = token.getGroups()[0];
                        // If no CVar was found return null
                        if (!ctx.hasCompilerVar(varName))
                            return null;

                        // If data is null return the value of the CVar
                        if (data == null)
                            return (double) ctx.getCompilerVar(varName).get();

                        // If data is not null then it must be an instance of CompilerVarStack
                        assert data instanceof CompilerVarStack;
                        CompilerVarStack varStack = (CompilerVarStack) data;
                        // If this var is in the stack then it's a circular reference because eventually we'll get back to here
                        if (varStack.isInStack(varName)) {
                            Token varDeclarationToken = varStack.pop().getToken();
                            throw new RuntimeException(StringUtils.format(
                                    "'{0}' produces Circular Reference to '{1}' declared at ({2}:{3})",
                                    token.getMatch(), varDeclarationToken.getMatch(),
                                    varDeclarationToken.getLine(), varDeclarationToken.getLineChar()
                            ));
                        }

                        // Return the value of the CVar using the current Var Stack
                        return (double) ctx.getCompilerVar(varName).get(varStack);
                    },
                    ctx.getCurrentFile(), currentToken.getLine(), currentToken.getLineChar() + tokenGroups[0].length()
            );

            valueSupplier = data -> mathSupplier.get(data).intValue();
        } else return new ParseResult<>();

        if (addNodes) ctx.addNode(new ValueNode(valueSupplier));
        return new ParseResult<>(true, valueSupplier);
    }

    private static char strCodePointToChar(@NotNull ParserContext ctx, @NotNull String str) {
        boolean isCodePointValid = false;

        int codePoint = 0;
        try {
            codePoint = Integer.parseUnsignedInt(str);
            isCodePointValid = Character.isValidCodePoint(codePoint);
        } catch (Exception ignored) { }

        if (!isCodePointValid)
            throw new ParserError.SyntaxError(ctx, "Char Code Point", str);

        return (char) codePoint;
    }

    private static @NotNull String unescapeString(@NotNull ParserContext ctx, @NotNull String str) {
        StringBuilder strBuilder = new StringBuilder(str.length());

        boolean isEscaping = false;
        StringBuilder codePointBuilder = new StringBuilder(4);

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (isEscaping) {
                if (Character.isDigit(currentChar)) {
                    codePointBuilder.append(currentChar);
                } else if (codePointBuilder.length() > 0) {
                    strBuilder.append(
                            strCodePointToChar(ctx, codePointBuilder.toString())
                    );
                    codePointBuilder.setLength(0);

                    if (currentChar != ESCAPE_TERM)
                        strBuilder.append(currentChar);
                    isEscaping = false;
                } else {
                    strBuilder.append(
                        StringUtils.SpecialCharacters.MAP.getOrDefault(currentChar, currentChar)
                    );
                    isEscaping = false;
                }
            } else if (currentChar == ESCAPE_CHAR)
                isEscaping = true;
            else strBuilder.append(currentChar);
        }

        if (codePointBuilder.length() > 0) {
            strBuilder.append(
                    strCodePointToChar(ctx, codePointBuilder.toString())
            );
        }

        return strBuilder.toString();
    }

    private static @NotNull ParseResult<IPIntSupplier> parseCharacter(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();

        int value;
        if (CHARACTER.isDefinitionOf(currentToken)) {
            assert currentToken != null;

            String rawChar = currentToken.getGroups()[0];
            String character = unescapeString(ctx, rawChar);

            if (character.length() != 1)
                throw new ParserError.SyntaxError(ctx, CHARACTER.getName(), rawChar);
            value = character.charAt(0);
        } else return new ParseResult<>();

        IPIntSupplier valueSupplier = data -> value;
        if (addNodes) ctx.addNode(new ValueNode(valueSupplier));

        return new ParseResult<>(true, valueSupplier);
    }

    private static @NotNull ParseResult<String> parseString(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();
        if (!STRING.isDefinitionOf(currentToken))
            return new ParseResult<>();
        assert currentToken != null;

        String str = unescapeString(ctx, currentToken.getGroups()[0]);
        if (addNodes) ctx.addNode(new StringNode(str));

        return new ParseResult<>(true, str);
    }

    private static @NotNull ParseResult<IPIntSupplier> parseRegister(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();
        if (!IDENTIFIER.isDefinitionOf(currentToken))
            return new ParseResult<>();
        assert currentToken != null;

        String regName = currentToken.getMatch();
        int address = -1;

        IRegister register = ctx.processor.getRegister(regName);
        if (register == null) {
            IFlag flag = ctx.processor.getFlag(regName);
            if (flag != null) {
                if (!(flag instanceof IMemoryFlag))
                    throw new ParserError.ProcessorError(ctx, "Reading/Writing to Flag \"" + regName + "\" isn't supported!");
                address = ((IMemoryFlag) flag).getAddress();
            }
        } else {
            if (!(register instanceof IMemoryRegister))
                throw new ParserError.ProcessorError(ctx, "Reading/Writing to Register \"" + regName + "\" isn't supported!");
            address = ((IMemoryRegister) register).getAddress();
        }

        if (address < 0) return new ParseResult<>();

        if (addNodes)
            ctx.addNode(new RegisterNode(address, regName));

        int finalAddress = address; // IntelliJ would get mad at me if I didn't put this here, don't know why tho
        return new ParseResult<>(true, data -> finalAddress);
    }

    private static @NotNull ParseResult<IPIntSupplier> parseCompilerVar(@NotNull ParserContext ctx, boolean isGetting, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();
        if (!C_VAR.isDefinitionOf(currentToken))
            return new ParseResult<>();
        assert currentToken != null;

        String varName = currentToken.getGroups()[0];
        if (isGetting) {
            if (!ctx.hasCompilerVar(varName))
                throw new ParserError.ReferenceError(ctx, C_VAR.getName(), varName, "was not declared.");

            IPIntSupplier valueSupplier = data -> ctx.getCompilerVar(varName).get(data);
            if (addNodes) ctx.addNode(new ValueNode(valueSupplier));
            return new ParseResult<>(true, valueSupplier);
        }

        Token staticValToken = ctx.tokenizer.goForward();

        ParseResult<IPIntSupplier> staticValue = parseBasicValue(ctx, false);
        if (!staticValue.SUCCESS)
            throw new ParserError.SyntaxError(ctx, BASIC_TYPES, formatToken(staticValToken));

        CompilerVarDeclaration varDeclaration = new CompilerVarDeclaration(varName, currentToken);
        IPIntSupplier valueSupplier = data -> {
            if (data == null) {
                CompilerVarStack varStack = new CompilerVarStack(varDeclaration);
                return staticValue.VALUE.get(varStack);
            }

            assert data instanceof CompilerVarStack;
            CompilerVarStack varStack = (CompilerVarStack) data;
            varStack.push(varDeclaration);
            int value = staticValue.VALUE.get(data);
            varStack.pop();
            return value;
        };

        ctx.putCompilerVar(varName, valueSupplier);
        return new ParseResult<>(true, valueSupplier);
    }

    private static @NotNull ParseResult<IPIntSupplier> parseBasicValue(@NotNull ParserContext ctx, boolean addNodes) {
        ParseResult<IPIntSupplier> result;

        result = parseNumber(ctx, addNodes);
        if (!result.SUCCESS) result = parseMathExpr(ctx, addNodes);
        if (!result.SUCCESS) result = parseCharacter(ctx, addNodes);
        if (!result.SUCCESS) result = parseCompilerVar(ctx, true, addNodes);

        return result;
    }

    private static int parseArgument(@NotNull ParserContext ctx, boolean allowLabelDeclaration) {
        if (
                parseOffset(ctx, true).SUCCESS ||
                parseRegister(ctx, true).SUCCESS ||
                parseBasicValue(ctx, true).SUCCESS
        ) return 1;

        int labelParseResult = parseLabel(ctx, allowLabelDeclaration, true);
        if (labelParseResult > 0) return 1;
        else if (labelParseResult == 0) return 0;

        return -1;
    }

    private static @NotNull ParseResult<IPIntSupplier> parseOffset(@NotNull ParserContext ctx, boolean addNodes) {
        Token currentToken = ctx.tokenizer.getCurrentToken();
        if (!L_BRACKET.isDefinitionOf(currentToken))
            return new ParseResult<>();

        Token staticValToken = ctx.tokenizer.goForward();
        ParseResult<IPIntSupplier> staticValue = parseBasicValue(ctx, false);
        if (!staticValue.SUCCESS)
            throw new ParserError.SyntaxError(ctx, BASIC_TYPES, formatToken(staticValToken));
        if (addNodes) ctx.addNode(new OffsetNode(staticValue.VALUE));

        currentToken = ctx.tokenizer.goForward();
        if (!R_BRACKET.isDefinitionOf(currentToken))
            throw new ParserError.SyntaxError(ctx, L_BRACKET.getName(), formatToken(currentToken));

        return new ParseResult<>(true, staticValue.VALUE);
    }

    private static boolean parseArray(@NotNull ParserContext ctx) {
        ParseResult<IPIntSupplier> arraySize = parseOffset(ctx, false);
        if (arraySize.SUCCESS) {
            ctx.addNode(new ArrayNode(arraySize.VALUE));
            return true;
        }

        if (!L_BRACE.isDefinitionOf(ctx.tokenizer.getCurrentToken()))
            return false;

        do {
            if (ctx.tokenizer.goForward() == null)
                throw new ParserError.SyntaxError(ctx, R_BRACE.getName(), formatToken(null));
        } while (parseArgument(ctx, true) >= 0);

        Token arrayEndToken = ctx.tokenizer.getCurrentToken();
        if (!R_BRACE.isDefinitionOf(arrayEndToken))
            throw new ParserError.SyntaxError(ctx, R_BRACE.getName(), formatToken(arrayEndToken));
        return true;
    }

    /**
     * Parses a Label<br>
     * NOTE: THIS ALWAYS ADDS A NODE
     * @param ctx The {@link ParserContext}
     * @param canDeclare Whether or not a Label can be Declared
     * @param canUse Whether or not a Label can be Used
     * @return -1 if parsing failed, 0 if a Label was Declared, 1 if it was used
     */
    private static int parseLabel(@NotNull ParserContext ctx, boolean canDeclare, boolean canUse) {
        Token labelToken = ctx.tokenizer.getCurrentToken();
        if (IDENTIFIER.isDefinitionOf(labelToken)) {
            assert labelToken != null;

            String labelName = labelToken.getMatch();
            boolean isDeclaration =
                    LABEL_DECL.isDefinitionOf(ctx.tokenizer.peekForward());

            if (isDeclaration) {
                ctx.tokenizer.goForward();
                if (!canDeclare)
                    throw new ParserError.TypeError(ctx, "Label Declaration isn't allowed here.");
            } else if (!canUse) {
                throw new ParserError.TypeError(ctx, "Label Usage isn't allowed here.");
            }

            ctx.addNode(
                    new LabelNode(ctx, labelName, 0, isDeclaration)
            );

            return isDeclaration ? 0 : 1;
        } else if (LABEL_OFF.isDefinitionOf(labelToken)) {
            assert labelToken != null;

            if (!canUse)
                throw new ParserError.TypeError(ctx, "Label Usage isn't allowed here.");


            Token offsetToken = ctx.tokenizer.goForward();
            ParseResult<IPIntSupplier> offset = parseBasicValue(ctx, false);
            if (!offset.SUCCESS)
                throw new ParserError.SyntaxError(ctx, BASIC_TYPES, formatToken(offsetToken));

            Token endBracket = ctx.tokenizer.goForward();
            if (!R_BRACKET.isDefinitionOf(endBracket))
                throw new ParserError.SyntaxError(ctx, R_BRACKET.getName(), formatToken(endBracket));

            String labelName = labelToken.getGroups()[0];
            ctx.addNode(
                    new LabelNode(ctx, labelName, offset.VALUE, false)
            );

            return 1;
        }

        return -1;
    }

    private static @NotNull ParseResult<Instruction> parseInstruction(@NotNull ParserContext ctx) {
        Token currentToken = ctx.tokenizer.getCurrentToken();
        if (!IDENTIFIER.isDefinitionOf(currentToken))
            return new ParseResult<>();
        assert currentToken != null;

        InstructionSet instructionSet = ctx.processor.getInstructionSet();
        int opcode = instructionSet.getOpcode(
                currentToken.getMatch()
        );

        if (opcode < 0)
            return new ParseResult<>();

        Instruction instruction = instructionSet.getInstruction(opcode);
        assert instruction != null;

        ctx.addNode(new InstructionNode(opcode, instruction));
        for (int i = 0; i < instruction.getArgumentsCount();) {
            Token currentArgToken = ctx.tokenizer.goForward();

            int parseArgumentResult = parseArgument(ctx, true);
            if (parseArgumentResult > 0) i++;
            else if (parseArgumentResult < 0)
                throw new ParserError.ArgumentsError(ctx, instruction.getKeyword(), i, ARGUMENT_TYPES, formatToken(currentArgToken));
        }

        return new ParseResult<>(true, instruction);
    }

    private static @NotNull List<INode> internalParseFile(@NotNull IProcessor processor, @NotNull File srcFile, @NotNull HashSet<String> includedFiles, @NotNull CompilerVars cVars) {
        String srcFilePath = FileUtils.tryGetCanonicalPath(srcFile);
        if (includedFiles.contains(srcFilePath))
            return new ArrayList<>(0);
        includedFiles.add(srcFilePath);

        String src;
        try {
            src = new String(Files.readAllBytes(srcFile.toPath()), StandardCharsets.UTF_8);
        } catch (Exception err) {
            throw new ParserError.FileError(srcFile, "Something went wrong when reading file.");
        }

        ParserContext ctx = new ParserContext(
                processor, srcFile, new Tokenizer(src, ALL_DEFINITIONS), cVars
        );

        ctx.tokenizer.removeTokensByDefinition(
                COMMENT, NEWLINE, SPACE
        );

        while (ctx.tokenizer.canGoForward()) {
            Token currentToken = ctx.tokenizer.goForward();

            if (parseInstruction(ctx).SUCCESS)
                continue;

            if (C_INSTR.isDefinitionOf(currentToken)) {
                assert currentToken != null;
                String instrName = currentToken.getGroups()[0];

                Token instrArg = ctx.tokenizer.goForward();
                switch (instrName) {
                    case C_INSTR_DEFINE_WORD:
                        if (parseArgument(ctx, false) <= 0)
                            throw new ParserError.SyntaxError(ctx, ARGUMENT_TYPES, formatToken(instrArg));
                        break;
                    case C_INSTR_DEFINE_STRING:
                        if (!parseString(ctx, true).SUCCESS)
                            throw new ParserError.SyntaxError(ctx, STRING.getName(), formatToken(instrArg));
                        break;
                    case C_INSTR_DEFINE_ARRAY:
                        if (!parseArray(ctx))
                            throw new ParserError.SyntaxError(ctx, "Array", formatToken(instrArg));
                        break;
                    case C_INSTR_INCLUDE:
                        ParseResult<String> parsedPath = parseString(ctx, false);
                        if (!parsedPath.SUCCESS)
                            throw new ParserError.SyntaxError(ctx, "Include File Path", formatToken(instrArg));
                        String includePath = parsedPath.VALUE;

                        File includeFile;
                        try {
                            includeFile = new File(srcFile.toURI().resolve(includePath));
                        } catch (Exception err) {
                            throw new ParserError.SyntaxError(ctx, "Valid Include Path", includePath);
                        }

                        if (!includeFile.exists())
                            throw new ParserError.FileError(ctx, "Couldn't include file because it doesn't exist");
                        if (!includeFile.canRead())
                            throw new ParserError.FileError(ctx, "Couldn't include file because it can't be read");

                        ctx.addNodes(
                                internalParseFile(ctx.processor, includeFile, includedFiles, cVars)
                        );
                        break;
                    default:
                        // This should never throw
                        throw new ParserError.SyntaxError(ctx, C_INSTR.getName(), instrName);
                }

                continue;
            }

            if (
                    !parseCompilerVar(ctx, false, false).SUCCESS &&
                    parseLabel(ctx, true, false) != 0
            ) throw new ParserError.SyntaxError(ctx, GENERIC_TYPES, formatToken(currentToken));
        }

        return ctx.getNodes();
    }

    public static @NotNull List<INode> parseFile(@NotNull File srcFile, @NotNull IProcessor processor) {
        if (!srcFile.exists())
            throw new ParserError.FileError(srcFile, "Couldn't compile file because it doesn't exist");
        if (!srcFile.canRead())
            throw new ParserError.FileError(srcFile, "Couldn't compile file because it can't be read");
        return Parser.internalParseFile(processor, srcFile, new HashSet<>(), CompilerVars.getDefaultVars());
    }
}
