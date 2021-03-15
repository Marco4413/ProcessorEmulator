package io.github.hds.pemu.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtils {

    public static int parseInt(@NotNull String str) {
        int radix = 10;
        String number = str;
        if (number.startsWith("0x")) radix = 16;
        else if (number.startsWith("0o")) radix =  8;
        else if (number.startsWith("0b")) radix =  2;
        if (radix != 10) number = number.substring(2);
        return Integer.parseInt(number, radix);
    }

    public static @NotNull String toShortName(@NotNull String name) {
        StringBuilder shortName = new StringBuilder();
        String[] words = name.split("\\s+");
        for (String word : words) {
            shortName.append( word.charAt(0) );
        }

        return shortName.toString().toUpperCase();
    }

    public static @NotNull String stackTraceAsString(@NotNull Exception err) {
        StringWriter str = new StringWriter();
        err.printStackTrace(new PrintWriter(str));
        return str.toString();
    }

}
