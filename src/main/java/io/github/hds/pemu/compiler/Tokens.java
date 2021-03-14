package io.github.hds.pemu.compiler;

public class Tokens {

    public static final Token COMMENT  = new Token(";");
    public static final Token LABEL    = new Token(":");
    public static final Token COMPILER = new Token("#");
    public static final Token STRING   = new Token("[\"']");
    public static final Token ESCAPECH = new Token("\\\\");
    public static final Token CONSTANT = new Token("@");
    public static final Token SPACE    = new Token("\\s");

    public static final Token[] ALL_TOKENS = new Token[] { COMMENT, LABEL, COMPILER, CONSTANT, STRING, ESCAPECH, SPACE };

}
