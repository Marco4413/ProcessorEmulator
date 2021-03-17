package io.github.hds.pemu.compiler;

import java.util.HashMap;

public class SpecialCharacters {
    public static final HashMap<Character, Character> SPECIAL_MAP = new HashMap<>();

    static {
        SPECIAL_MAP.put('\'', '\''); // Quotes
        SPECIAL_MAP.put('\"', '\"'); // Double quotes
        SPECIAL_MAP.put('\\', '\\'); // Backslash
        SPECIAL_MAP.put('t' , '\t'); // Tab character
        SPECIAL_MAP.put('b' , '\b'); // Backspace, displays as an unknown character on the console and doesn't do anything
        SPECIAL_MAP.put('r' , '\r'); // Carriage return, doesn't display on the console and doesn't do anything
        SPECIAL_MAP.put('f' , '\f'); // Form feed, displays as an unknown character on the console and doesn't do anything
        SPECIAL_MAP.put('n' , '\n'); // Newline
        SPECIAL_MAP.put('0' , '\0'); // NULL
    }
}
