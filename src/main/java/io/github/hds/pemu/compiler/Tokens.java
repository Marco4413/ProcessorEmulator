package io.github.hds.pemu.compiler;

public class Tokens {

    public static final Token COMMENT  = new Token(";");
    public static final Token LABEL    = new Token(":");
    public static final Token CONSTANT = new Token("@");
    public static final Token INSTR    = new Token(".");
    public static final Token SPACE    = new Token("\\s");
    public static final Token COMMA    = new Token(",");

    public static final Token[] TOKENIZER_FILTER = new Token[] { COMMENT, LABEL, CONSTANT, SPACE, COMMA };
    public static final Token[] DELIMITERS = new Token[] { SPACE, COMMA };

}
