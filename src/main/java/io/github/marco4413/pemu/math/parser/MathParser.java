package io.github.marco4413.pemu.math.parser;

import io.github.marco4413.pemu.tokenizer.ITokenDefinition;
import io.github.marco4413.pemu.tokenizer.Token;
import io.github.marco4413.pemu.tokenizer.TokenDefinition;
import io.github.marco4413.pemu.tokenizer.Tokenizer;
import io.github.marco4413.pemu.utils.IDoubleSupplier;
import io.github.marco4413.pemu.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class MathParser {

    @FunctionalInterface
    private interface BiOperator {
        Double apply(Double a, Double b) throws Exception;
    }

    @FunctionalInterface
    private interface MonoOperator {
        Double apply(Double a) throws Exception;
    }

    private static final HashMap<ITokenDefinition, BiOperator> BI_OPERATORS = new HashMap<>();
    private static final HashMap<ITokenDefinition, MonoOperator> MONO_OPERATORS = new HashMap<>();

    private static final String BI_OPERATOR_ERROR = "Operator \"{0}\" Produced {1}: {2} {0} {3}";
    private static @NotNull TokenDefinition createBiOperator(@NotNull String name, @NotNull String keyword, @NotNull BiOperator operator, boolean checkResult) {
        TokenDefinition tokenDefinition = new TokenDefinition(name, keyword, true);

        BiOperator operatorToAdd = operator;
        if (checkResult) {
            operatorToAdd = (x, y) -> {
                Double value = operator.apply(x, y);
                if (value == null)
                    throw new Exception(StringUtils.format(BI_OPERATOR_ERROR, keyword, "Null Value", x, y));
                else if (Double.isNaN(value))
                    throw new Exception(StringUtils.format(BI_OPERATOR_ERROR, keyword, "NaN", x, y));
                else if (Double.isInfinite(value))
                    throw new Exception(StringUtils.format(BI_OPERATOR_ERROR, keyword, "Infinite Number", x, y));
                return value;
            };
        }

        BI_OPERATORS.put(tokenDefinition, operatorToAdd);
        return tokenDefinition;
    }

    private static @NotNull TokenDefinition createBiOperator(@NotNull String name, @NotNull String keyword, @NotNull BiOperator operator) {
        return createBiOperator(name, keyword, operator, false);
    }

    private static final String MONO_OPERATOR_ERROR = "Operator \"{0}\" Produced {1}: {0}({2})";
    private static @NotNull TokenDefinition createMonoOperator(@NotNull String name, @NotNull String keyword, @NotNull MonoOperator operator, boolean checkResult) {
        TokenDefinition tokenDefinition = new TokenDefinition(name, keyword, true);

        MonoOperator operatorToAdd = operator;
        if (checkResult) {
            operatorToAdd = (x) -> {
                Double value = operator.apply(x);
                if (value == null)
                    throw new Exception(StringUtils.format(MONO_OPERATOR_ERROR, keyword, "Null Value", x));
                else if (Double.isNaN(value))
                    throw new Exception(StringUtils.format(MONO_OPERATOR_ERROR, keyword, "NaN", x));
                else if (Double.isInfinite(value))
                    throw new Exception(StringUtils.format(MONO_OPERATOR_ERROR, keyword, "Infinite Number", x));
                return value;
            };
        }

        MONO_OPERATORS.put(tokenDefinition, operatorToAdd);
        return tokenDefinition;
    }

    private static @NotNull TokenDefinition createMonoOperator(@NotNull String name, @NotNull String keyword, @NotNull MonoOperator operator) {
        return createMonoOperator(name, keyword, operator, false);
    }

    private static final TokenDefinition HEX_NUMBER = new TokenDefinition("Hex Number", "0x[0-9a-fA-F]+");
    private static final TokenDefinition OCT_NUMBER = new TokenDefinition("Octal Number", "0o[0-7]+");
    private static final TokenDefinition BIN_NUMBER = new TokenDefinition("Binary Number", "0b[01]+");
    private static final TokenDefinition FLO_NUMBER = new TokenDefinition("Floating Number", "[0-9]*\\.[0-9]+");
    private static final TokenDefinition INT_NUMBER = new TokenDefinition("Integer Number", "[0-9]+");
    private static final TokenDefinition PLUS = new TokenDefinition("Plus", "+", true);
    private static final TokenDefinition MINUS = new TokenDefinition("Minus", "-", true);
    private static final TokenDefinition L_PARENTHESIS = new TokenDefinition("Left Parenthesis", "(", true);
    private static final TokenDefinition R_PARENTHESIS = new TokenDefinition("Right Parenthesis", ")", true);
    private static final TokenDefinition WHITESPACE = new TokenDefinition("Whitespace", "\\s+");

    private static final ITokenDefinition[] ALL_TOKENS = new ITokenDefinition[] {
            HEX_NUMBER, OCT_NUMBER, BIN_NUMBER,
            FLO_NUMBER, INT_NUMBER,
            L_PARENTHESIS, R_PARENTHESIS,
            PLUS, MINUS,
            createMonoOperator("Absolute Value", "abs", Math::abs, true),
            createMonoOperator("Square Root"   , "sqrt", Math::sqrt, true),
            createMonoOperator("Percentage"    , "%", x -> x / 100.0d),
            createMonoOperator("Bitwise NOT"   , "~", x -> (double) (~x.longValue())),
            createMonoOperator("Logarithm Base 2" , "log2", x -> Math.log10(x) / Math.log10(2)),
            createMonoOperator("Logarithm Base 10", "log", Math::log10),
            createBiOperator("Multiply", "*" , (a, b) -> a * b),
            createBiOperator("Divide"  , "/" , (a, b) -> {
                if (b == 0.0d) throw new Exception("Division by 0");
                return a / b;
            }),
            createBiOperator("Power"      , "pow", Math::pow, true),
            createBiOperator("Shift Left" , "<<" , (x, n) -> (double) (x.longValue() << n.longValue())),
            createBiOperator("Shift Right", ">>" , (x, n) -> (double) (x.longValue() >> n.longValue())),
            createBiOperator("Bitwise OR" , "|"  , (x, n) -> (double) (x.longValue() | n.longValue())),
            createBiOperator("Bitwise AND", "&"  , (x, n) -> (double) (x.longValue() & n.longValue())),
            createBiOperator("Bitwise XOR", "^"  , (x, n) -> (double) (x.longValue() ^ n.longValue())),
            WHITESPACE
    };

    private static boolean isIntNumber(@Nullable Token token) {
        return HEX_NUMBER.isDefinitionOf(token) || OCT_NUMBER.isDefinitionOf(token) ||
                BIN_NUMBER.isDefinitionOf(token) || INT_NUMBER.isDefinitionOf(token);
    }

    private static boolean isFloNumber(@Nullable Token token) {
        return FLO_NUMBER.isDefinitionOf(token);
    }

    private static boolean isNumber(@Nullable Token token) {
        return isIntNumber(token) || isFloNumber(token);
    }

    private static double toNumber(@NotNull Token token) {
        if (isIntNumber(token))
            return StringUtils.parseLong(token.getMatch());
        else if (isFloNumber(token))
            return Double.parseDouble(token.getMatch());
        // We should never get here because isNumber should be called before this to check if token is valid
        throw new IllegalStateException("Use isNumber to check if Token is NaN before calling toNumber");
    }

    private static @Nullable IDoubleSupplier parseNumber(@NotNull ParserContext ctx, boolean requireSign) {
        Token signToken = ctx.tokenizer.getCurrentToken();
        if (signToken == null) return null;

        boolean hasSign = PLUS.isDefinitionOf(signToken) || MINUS.isDefinitionOf(signToken);
        if (requireSign && !hasSign)
            throw new ParserError.SyntaxError(ctx, "Sign", signToken.getMatch());

        int signFactor = !hasSign || PLUS.isDefinitionOf(signToken) ? 1 : -1;
        Token numberToken = hasSign ? ctx.tokenizer.goForward() : signToken;
        if (numberToken == null)
            throw new ParserError.SyntaxError(ctx, "Number", ctx.tokenizer.getCurrentToken().getMatch());

        if (L_PARENTHESIS.isDefinitionOf(numberToken)) {
            IDoubleSupplier scopeNumber = parseScope(ctx, false);
            return (data) -> scopeNumber.get(data) * signFactor;
        } else if (isNumber(numberToken)) {
            double number = toNumber(numberToken) * signFactor;
            return (data) -> number;
        } else if (ctx.VAR_DEFINITION.isDefinitionOf(numberToken)) {
            int currentLine = ctx.getCurrentLine();
            int currentLineChar = ctx.getCurrentLineChar();
            return (data) -> {
                Double varValue;

                try {
                    varValue = ctx.VAR_PROCESSOR.apply(numberToken, data);
                } catch (ParserError err) {
                    throw err;
                } catch (Exception err) {
                    throw new ParserError.VariableError(ctx.SOURCE_FILE, currentLine, currentLineChar, err.getMessage());
                }

                if (varValue == null)
                    throw new ParserError.ReferenceError(ctx.SOURCE_FILE, currentLine, currentLineChar, "Variable", numberToken.getMatch(), "is not declared");
                return varValue * signFactor;
            };
        } else throw new ParserError.SyntaxError(ctx, "Number", numberToken.getMatch());
    }

    private static @Nullable IDoubleSupplier parseMonoOperator(@NotNull ParserContext ctx, boolean requireSign) {
        Token signToken = ctx.tokenizer.getCurrentToken();
        if (signToken == null) return null;

        boolean hasSign = PLUS.isDefinitionOf(signToken) || MINUS.isDefinitionOf(signToken);
        if (requireSign && !hasSign)
            throw new ParserError.SyntaxError(ctx, "Sign", signToken.getMatch());

        int signFactor = !hasSign || PLUS.isDefinitionOf(signToken) ? 1 : -1;
        Token operatorToken = hasSign ? ctx.tokenizer.peekForward() : signToken;
        if (operatorToken == null || !MONO_OPERATORS.containsKey(operatorToken.getDefinition()))
            return null;

        // Going to the Operator Token if no Sign is present
        if (hasSign) ctx.tokenizer.goForward();

        // Consuming the Operator Token to make parseNumber able to get the number after it
        ctx.tokenizer.goForward();
        IDoubleSupplier number = parseNumber(ctx, false);
        if (number == null)
            throw new ParserError.SyntaxError(ctx, "Number", ctx.tokenizer.getCurrentToken().getMatch());

        MonoOperator operator = MONO_OPERATORS.get(operatorToken.getDefinition());
        assert operator != null;

        int currentLine = ctx.getCurrentLine();
        int currentLineChar = ctx.getCurrentLineChar();
        return (data) -> {
            try {
                return operator.apply(number.get(data)) * signFactor;
            } catch (ParserError err) {
                throw err;
            } catch (Exception err) {
                throw new ParserError.OperatorError(ctx.SOURCE_FILE, currentLine, currentLineChar, err.getMessage());
            }
        };
    }

    private static @NotNull IDoubleSupplier parseScope(@NotNull ParserContext ctx, boolean isRoot) {
        ArrayList<IDoubleSupplier> numbers = new ArrayList<>();

        for (int i = 0; ctx.tokenizer.goForward() != null; i++) {
            Token currentToken = ctx.tokenizer.getCurrentToken();
            assert currentToken != null;

            if (R_PARENTHESIS.isDefinitionOf(currentToken)) {
                if (isRoot)
                    throw new ParserError.SyntaxError(ctx, "Operator, Variable or Number", "Scope Termination");
                break;
            } else if (BI_OPERATORS.containsKey(currentToken.getDefinition())) {
                ctx.tokenizer.goForward();
                IDoubleSupplier lastOperand = parseNumber(ctx, false);
                if (lastOperand == null)
                    throw new ParserError.SyntaxError(ctx, "Second Operand", currentToken.getMatch());
                IDoubleSupplier firstOperand = numbers.remove(numbers.size() - 1);

                BiOperator operator = BI_OPERATORS.get(currentToken.getDefinition());
                int currentLine = ctx.getCurrentLine();
                int currentLineChar = ctx.getCurrentLineChar();
                numbers.add((data) -> {
                    try {
                        return operator.apply(firstOperand.get(data), lastOperand.get(data));
                    } catch (ParserError err) {
                        throw err;
                    } catch (Exception err) {
                        throw new ParserError.OperatorError(ctx.SOURCE_FILE, currentLine, currentLineChar, err.getMessage());
                    }
                });
            } else {
                boolean signRequired = i > 0;
                IDoubleSupplier number = parseMonoOperator(ctx, signRequired);
                if (number == null) number = parseNumber(ctx, signRequired);
                if (number == null) throw new ParserError.SyntaxError(ctx, "Operator, Variable or Number", currentToken.getMatch());
                numbers.add(number);
            }
        }

        if (numbers.size() == 0)
            throw new ParserError.SyntaxError(ctx, "Expression", "Empty Scope");

        return (data) -> {
            double result = 0;
            for (IDoubleSupplier number : numbers)
                result += number.get(data);
            return result;
        };
    }

    public static @NotNull IDoubleSupplier parseMath(
            @NotNull String source, @NotNull ITokenDefinition varDef, @NotNull IVarProcessor varProcessor,
            @Nullable File sourceFile, int sourceLine, int sourceLineChar
    ) {
        ITokenDefinition[] definitions = Arrays.copyOf(ALL_TOKENS, ALL_TOKENS.length + 1);
        definitions[definitions.length - 1] = varDef;
        Tokenizer tokenizer = new Tokenizer(source, definitions);

        tokenizer.removeTokensByDefinition(WHITESPACE);
        return parseScope(
                new ParserContext(
                        tokenizer, varDef, varProcessor,
                        sourceFile, sourceLine, sourceLineChar
                ), true
        );
    }

    public static @NotNull IDoubleSupplier parseMath(@NotNull String source, @NotNull ITokenDefinition varDef, @NotNull IVarProcessor varProcessor, @Nullable File sourceFile) {
        return parseMath(source, varDef, varProcessor, sourceFile, 1, 1);
    }

    public static @NotNull IDoubleSupplier parseMath(@NotNull String source, @NotNull ITokenDefinition varDef, @NotNull IVarProcessor varProcessor) {
        return parseMath(source, varDef, varProcessor, null);
    }

}
