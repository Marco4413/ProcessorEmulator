package io.github.hds.pemu.tokenizer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TokenGroup {

    protected final ArrayList<Token> GROUP;

    public TokenGroup() {
        GROUP = new ArrayList<>();
    }

    public @NotNull Token[] getGroup() {
        return GROUP.toArray(new Token[0]);
    }

    public @NotNull TokenGroup addToken(@NotNull Token token) {
        String tokenPattern = token.getPattern();
        for (Token groupToken : GROUP)
            if (tokenPattern.equals(groupToken.getPattern()))
                return this;
        GROUP.add(token);
        return this;
    }

    public @NotNull TokenGroup addTokens(@NotNull Token ...tokens) {
        for (Token token : tokens)
            addToken(token);
        return this;
    }

}
