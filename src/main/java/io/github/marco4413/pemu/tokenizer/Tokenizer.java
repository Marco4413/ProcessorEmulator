package io.github.marco4413.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Tokenizer {

    public static final ITokenDefinition UNKNOWN = new TokenDefinition("Unknown", ".*");

    private int nextIndex = 0;
    private final ArrayList<Token> TOKENS;

    public Tokenizer(@NotNull String source, @NotNull ITokenDefinition... tokenDefinitions) {
        List<ITokenDefinition> definitionsList = new ArrayList<>();
        Collections.addAll(definitionsList, tokenDefinitions);
        this.TOKENS = Tokenizer.tokenize(source, definitionsList);
    }

    public Tokenizer(@NotNull String source, @NotNull List<ITokenDefinition> tokenDefinitions) {
        this.TOKENS = Tokenizer.tokenize(source, tokenDefinitions);
    }

    public boolean canGoForward() {
        return this.isValidIndex(nextIndex);
    }

    public @Nullable Token goForward() {
        return canGoForward() ? TOKENS.get(nextIndex++) : null;
    }

    public @Nullable Token peekForward() {
        return canGoForward() ? TOKENS.get(nextIndex) : null;
    }

    public @Nullable Token goForward(@NotNull ITokenDefinition... definitionsBlacklist) {
        return goForward(false, definitionsBlacklist);
    }

    public @Nullable Token peekForward(@NotNull ITokenDefinition... definitionsBlacklist) {
        return peekForward(false, definitionsBlacklist);
    }

    public @Nullable Token goForward(boolean doWhitelist, @NotNull ITokenDefinition... definitionsFilter) {
        while (true) {
            Token nextToken = goForward();
            if (nextToken == null) return null;

            if (Tokenizer.isTokenCompatible(nextToken, doWhitelist, definitionsFilter))
                return nextToken;
        }
    }

    public @Nullable Token peekForward(boolean doWhitelist, @NotNull ITokenDefinition... definitionsFilter) {
        for (int i = nextIndex; i < TOKENS.size(); i++) {
            Token currentToken = TOKENS.get(i);
            if (Tokenizer.isTokenCompatible(currentToken, doWhitelist, definitionsFilter))
                return currentToken;
        }
        return null;
    }

    public @Nullable Token getCurrentToken() {
        return this.isValidIndex(nextIndex - 1) ? TOKENS.get(nextIndex - 1) : null;
    }

    public int getCurrentLine() {
        Token currentToken = getCurrentToken();
        if (currentToken == null) return -1;
        return currentToken.getLine();
    }

    public int getCurrentLineChar() {
        Token currentToken = getCurrentToken();
        if (currentToken == null) return -1;
        return currentToken.getLineChar();
    }

    public int removeTokensByDefinition(@NotNull ITokenDefinition... definitions) {
        int removedTokens = 0;
        for (int i = 0; i < TOKENS.size(); i++) {
            if (Tokenizer.isTokenCompatible(TOKENS.get(i), true, definitions)) {
                if (i < nextIndex) nextIndex--;
                TOKENS.remove(i--);
                removedTokens++;
            }
        }
        return removedTokens;
    }

    public @NotNull Token[] getTokens() {
        return this.TOKENS.toArray(new Token[0]);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < TOKENS.size();
    }

    private static boolean isTokenCompatible(@NotNull Token token, boolean doWhitelist, @NotNull ITokenDefinition... definitionsFilter) {
        for (ITokenDefinition definition : definitionsFilter) {
            if (token.isOfDefinition(definition)) {
                return doWhitelist;
            }
        }

        return !doWhitelist;
    }

    private static @NotNull ArrayList<Token> tokenize(@NotNull String source, @NotNull List<ITokenDefinition> tokenDefinitions) {
        // remainingSource is what remains of source to be Tokenized
        StringBuilder remainingSource = new StringBuilder(source);
        // This List will contain all found Tokens
        ArrayList<Token> tokens = new ArrayList<>();

        // Putting all the specified tokenDefinitions into this ArrayList
        //  This is used for optimization as seen in the for loop below
        ArrayList<ITokenDefinition> definitions = new ArrayList<>(tokenDefinitions.size());
        definitions.addAll(tokenDefinitions);

        // These Integers are Updated with the latest line and line's char
        //  Basically these keep track of where we are in the file
        AtomicInteger currentLine     = new AtomicInteger(1);
        AtomicInteger currentLineChar = new AtomicInteger(1);

        while (remainingSource.length() > 0) {
            // This is the result of the best run of a Matcher on remainingSource
            //  ( "Best Run" means the one that's the closest to index 0 )
            MatchResult bestMatchResult = null;
            // This is the ITokenDefinition that produced bestMatchResult
            ITokenDefinition bestMatchDef = null;

            // Here we convert remainingSource to a String, this should improve performance
            //  Because we're not asking StringBuilder to create a new String for each iteration
            String srcToMatch = remainingSource.toString();
            for (int i = 0; i < definitions.size(); i++) {
                // Current Definition, Definition's Pattern and Pattern's Matcher against srcToMatch ( remainingSource )
                ITokenDefinition def = definitions.get(i);
                Pattern pattern = def.getPattern();
                Matcher matcher = pattern.matcher(srcToMatch);

                // If any Match was found in srcToMatch
                if (matcher.find()) {
                    // Get the Match's Start
                    int start = matcher.start();
                    // If either this was the first Match or this Match is better than the last one
                    if (bestMatchResult == null || start < bestMatchResult.start()) {
                        // Store Match result and ITokenDefinition
                        bestMatchResult = matcher.toMatchResult();
                        bestMatchDef    = def;

                        // If this Match was at the start of srcToMatch
                        //  then this is the absolute Best Match and we break
                        if (start == 0) break;
                    }
                // If no Match was found then remove the definition from the ArrayList,
                //  so that we won't try to Match it again
                } else definitions.remove(i--);

            }

            // If we didn't get any Match
            if (bestMatchResult == null) {
                // Add a Token equal to the srcToMatch
                //  ( This is a very small Optimization to not call remainingSource#toString )
                //  And Break because nothing else can be found
                tokens.add(new Token(
                        srcToMatch, UNKNOWN, currentLine.get(), currentLineChar.get()
                ));
                break;
            // If the Best Match is not at the start of the remainingSource
            } else if (bestMatchResult.start() > 0) {
                // Get the Unknown part of the remainingSource and
                //  add it to the Tokens as an Unknown Token
                String unknownString = remainingSource.substring(0, bestMatchResult.start());
                tokens.add(new Token(
                        unknownString, UNKNOWN, currentLine.get(), currentLineChar.get()
                ));

                // This is an helper function that moves the cursor based on the specified String
                //  To do that it iterates through each character of the String:
                //   This is not a concern because Regex is the most expensive
                //    Operation here so we don't really care about the performance of this
                Tokenizer.countLinesNChars(unknownString, currentLine, currentLineChar);
            }

            // Here we put the Matched String into a Token with its Groups ( Starting from Group 1 )
            String matchedString = bestMatchResult.group();
            tokens.add(new Token(
                    bestMatchResult.group(), Tokenizer.getGroups(bestMatchResult), bestMatchDef, currentLine.get(), currentLineChar.get()
            ));
            Tokenizer.countLinesNChars(matchedString, currentLine, currentLineChar);

            // Deleting Characters in remainingSource from 0 to the End of the Match
            remainingSource.delete(0, bestMatchResult.end());
        }

        return tokens;
    }

    private static @NotNull String[] getGroups(@NotNull MatchResult matchResult) {
        String[] res = new String[matchResult.groupCount()];
        for (int i = 0; i < res.length; i++)
            res[i] = matchResult.group(i + 1);
        return res;
    }

    private static void countLinesNChars(@NotNull String str, @NotNull AtomicInteger currentLine, @NotNull AtomicInteger currentChar) {
        for (int i = 0; i < str.length(); i++) {
            currentChar.incrementAndGet();
            if (str.charAt(i) == '\n') {
                currentLine.incrementAndGet();
                currentChar.set(1);
            }
        }
    }

}
