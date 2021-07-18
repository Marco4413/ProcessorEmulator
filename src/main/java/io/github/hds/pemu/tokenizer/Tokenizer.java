package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Tokenizer {

    private final String[] TOKENS;
    private int nextTokenIndex = 0;
    private int consumedCharacters = 0;
    private int consumedLineChars = 0;
    private int consumedLines = 0;

    public Tokenizer(@NotNull String str, @NotNull Token... tokens) {
        if (tokens.length == 0) {
            TOKENS = new String[0];
            return;
        }

        StringBuilder ruleBuilder = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++)
            ruleBuilder.append("(").append(tokens[i].getPattern()).append(")|");
        ruleBuilder.append("(").append(tokens[tokens.length - 1].getPattern()).append(")");

        Pattern pattern = Pattern.compile(ruleBuilder.toString());
        TOKENS = splitString(str, pattern);
    }

    public Tokenizer(@NotNull String str, @NotNull TokenGroup tokenGroup) {
        this(str, tokenGroup.getGroup());
    }

    private static @NotNull String[] splitString(@NotNull String str, @NotNull Pattern pattern) {
        ArrayList<String> tokens = new ArrayList<>();

        Matcher patternMatcher = pattern.matcher(str);
        int lastIndex = 0;
        while (patternMatcher.find()) {
            int matchStart = patternMatcher.start();
            int matchEnd   = patternMatcher.end();

            if (lastIndex < matchStart)
                tokens.add(str.substring(lastIndex, matchStart));

            tokens.add(
                    str.substring(matchStart, matchEnd)
            );

            lastIndex = matchEnd;
        }

        if (lastIndex < str.length())
            tokens.add(str.substring(lastIndex));

        return tokens.toArray(new String[0]);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < TOKENS.length;
    }

    public boolean hasNext() {
        return isValidIndex(nextTokenIndex);
    }

    public @Nullable String getLast() {
        int lastIndex = nextTokenIndex - 1;
        return isValidIndex(lastIndex) ? TOKENS[lastIndex] : null;
    }

    public @Nullable String peekNext() {
        return hasNext() ? TOKENS[nextTokenIndex] : null;
    }

    public @Nullable String peekNext(Token... blacklist) {
        return peekNext(false, blacklist);
    }

    public @Nullable String peekNext(boolean whitelist, Token... filter) {
        int offset = 0;
        while (offset + nextTokenIndex < TOKENS.length) {
            String token = TOKENS[offset++ + nextTokenIndex];
            if (token == null) return null;

            boolean isValid = true;
            for (Token filtered : filter) {
                if (!isValid) break;
                if (whitelist)
                    isValid = filtered.matches(token);
                else isValid = !filtered.matches(token);
            }
            if (isValid) return token;
        }
        return null;
    }

    public @Nullable String consumeNext() {
        if (hasNext()) {
            String nextToken = TOKENS[this.nextTokenIndex];
            consumedCharacters += nextToken.length();

            // This shouldn't impact performance too much
            //  also we'll probably never tokenize REALLY long strings
            // Oh nice, I wrote this before the change which
            //  Tokenizes the whole program at once, life is full of surprises
            //  though still, Java should be fast enough and programs can't be that long
            int lastNewline = -1;
            for (int i = 0; i < nextToken.length(); i++) {
                if (nextToken.charAt(i) == '\n') {
                    lastNewline = i;
                    consumedLines++;
                }
            }

            if (lastNewline == -1)
                consumedLineChars += nextToken.length();
            else consumedLineChars = nextToken.length() - lastNewline;

            return TOKENS[this.nextTokenIndex++];
        }
        return null;
    }

    public @Nullable String consumeNext(Token... blacklist) {
        return consumeNext(false, blacklist);
    }

    public @Nullable String consumeNext(boolean whitelist, Token... filter) {
        while (hasNext()) {
            String token = consumeNext();
            if (token == null) return null;

            boolean isValid = true;
            for (Token filtered : filter) {
                if (!isValid) break;
                if (whitelist)
                    isValid = filtered.matches(token);
                else isValid = !filtered.matches(token);
            }
            if (isValid) return token;
        }
        return null;
    }

    public int getConsumedCharacters() {
        return consumedCharacters;
    }

    public int getConsumedLineCharacters() {
        return consumedLineChars;
    }

    public int getConsumedLines() {
        return consumedLines;
    }

}
