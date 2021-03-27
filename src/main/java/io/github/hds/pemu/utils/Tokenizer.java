package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Tokenizer {

    private String[] tokens;
    private int nextToken = 0;
    private int consumedCharacters = 0;

    public Tokenizer(@NotNull String str, boolean keepDelimiters, @NotNull String... delimiters) {
        String rule = String.join("", delimiters);
        String regex = keepDelimiters ?
                "((?<=[" + rule + "])|(?=[" + rule + "]))" :
                "[" + rule + "]+";
        tokens = str.split(regex);
    }

    public Tokenizer(@NotNull String str, boolean keepDelimiters, @NotNull Token... tokens) {
        StringBuilder ruleBuilder = new StringBuilder();
        for (Token token : tokens) {
            ruleBuilder.append(token.REGEX);
        }

        String rule = ruleBuilder.toString();
        String regex = keepDelimiters ?
                "((?<=[" + rule + "])|(?=[" + rule + "]))" :
                "[" + rule + "]+";
        this.tokens = str.split(regex);
    }

    public void removeDuplicates() {
        ArrayList<String> newTokens = new ArrayList<>();
        int offset = 0;

        String lastToken = "";
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (!lastToken.equals(token))
                newTokens.add(token);
            else if (i <= nextToken)
                offset++;

            lastToken = token;
        }

        nextToken -= offset;
        if (nextToken < 0)
            nextToken = 0;

        tokens = newTokens.toArray(new String[0]);
    }

    public void removeEmpties() {
        ArrayList<String> newTokens = new ArrayList<>();

        for (String token : tokens) {
            if (!token.equals(""))
                newTokens.add(token);
        }

        tokens = newTokens.toArray(new String[0]);
    }

    public boolean hasNext() {
        return nextToken < tokens.length;
    }

    public @Nullable String getLast() {
        return nextToken - 1 > 0 ? tokens[nextToken - 1] : null;
    }

    public @Nullable String peekNext() {
        return hasNext() ? tokens[nextToken] : null;
    }

    public @Nullable String peekNext(Token... tokenBlacklist) {
        int offset = 0;
        while (offset + nextToken < tokens.length) {
            String token = tokens[offset++ + nextToken];
            if (token == null) return null;

            boolean isValid = true;
            for (Token blacklisted : tokenBlacklist) {
                if (!isValid) break;

                isValid = !blacklisted.equals(token);
            }
            if (isValid) return token;
        }
        return null;
    }

    public @Nullable String consumeNext() {
        if (hasNext()) {
            consumedCharacters += tokens[nextToken].length();
            return tokens[nextToken++];
        }
        return null;
    }

    public @Nullable String consumeNext(Token... tokenBlacklist) {
        while (hasNext()) {
            String token = consumeNext();
            if (token == null) return null;

            boolean isValid = true;
            for (Token blacklisted : tokenBlacklist) {
                if (!isValid) break;

                isValid = !blacklisted.equals(token);
            }
            if (isValid) return token;
        }
        return null;
    }

    public int getConsumedCharacters() {
        return consumedCharacters;
    }

}
