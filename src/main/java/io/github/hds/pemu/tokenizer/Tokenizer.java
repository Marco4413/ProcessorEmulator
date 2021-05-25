package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Tokenizer {

    private String[] tokens;
    private int nextToken = 0;
    private int consumedCharacters = 0;
    private int consumedLineChars = 0;
    private int consumedLines = 0;

    public Tokenizer() {
        tokens = new String[0];
    }

    public Tokenizer(@NotNull String str, boolean keepTokens, @NotNull Token... tokens) {
        StringBuilder ruleBuilder = new StringBuilder();
        for (Token token : tokens)
            ruleBuilder.append(token.getPattern());

        String rule = ruleBuilder.toString();
        String regex = keepTokens ?
                "((?<=[" + rule + "])|(?=[" + rule + "]))" :
                "[" + rule + "]+";
        this.tokens = str.split(regex);
    }

    public void removeDuplicates() {
        ArrayList<String> newTokens = new ArrayList<>();
        int removed = 0;

        String lastToken = "";
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (!lastToken.equals(token)) newTokens.add(token);
            else if (i <= nextToken) removed++;
            lastToken = token;
        }

        nextToken -= removed;
        if (nextToken < 0) nextToken = 0;

        tokens = newTokens.toArray(new String[0]);
    }

    public void removeEmpties() {
        ArrayList<String> newTokens = new ArrayList<>();
        int removed = 0;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (!token.equals("")) newTokens.add(token);
            else if (i <= nextToken) removed++;
        }

        nextToken -= removed;
        if (nextToken < 0) nextToken = 0;

        tokens = newTokens.toArray(new String[0]);
    }

    public boolean hasNext() {
        return nextToken < tokens.length;
    }

    public @Nullable String getLast() {
        return nextToken - 1 >= 0 ? tokens[nextToken - 1] : null;
    }

    public @Nullable String peekNext() {
        return hasNext() ? tokens[nextToken] : null;
    }

    public @Nullable String peekNext(Token... ignoredTokens) {
        int offset = 0;
        while (offset + nextToken < tokens.length) {
            String token = tokens[offset++ + nextToken];
            if (token == null) return null;

            boolean isValid = true;
            for (Token ignored : ignoredTokens) {
                if (!isValid) break;

                isValid = !ignored.matches(token);
            }
            if (isValid) return token;
        }
        return null;
    }

    public @Nullable String consumeNext() {
        if (hasNext()) {
            String nextToken = tokens[this.nextToken];
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

            return tokens[this.nextToken++];
        }
        return null;
    }

    public @Nullable String consumeNext(Token... ignoredTokens) {
        while (hasNext()) {
            String token = consumeNext();
            if (token == null) return null;

            boolean isValid = true;
            for (Token ignored : ignoredTokens) {
                if (!isValid) break;

                isValid = !ignored.matches(token);
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
